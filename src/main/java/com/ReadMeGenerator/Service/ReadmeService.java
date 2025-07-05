package com.ReadMeGenerator.Service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

@Service
public class ReadmeService {

    @Value("${GROQ_API_KEY}")
    private String GROQ_API_KEY;

    public String generateReadmeFromRepo(String repoUrl) {
        String tmpDir = System.getProperty("java.io.tmpdir") + "/repo-" + UUID.randomUUID();
        try {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(tmpDir))
                    .call();

            String projectType = detectProjectType(tmpDir);
            String summary = summarizeCode(tmpDir);
            String prompt = buildPrompt(summary, projectType);
            String markdown = callGroq(prompt);

            // ✨ Inject emojis if Groq doesn't include them
            markdown = injectEmojis(markdown);

            return markdown;

        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        } finally {
            try {
                deleteDirectory(Paths.get(tmpDir));
            } catch (Exception ignored) {}
        }
    }

    private String callGroq(String prompt) throws IOException {



        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
        conn.setDoOutput(true);

        String body = "{"
                + "\"model\":\"llama3-8b-8192\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"You are a professional README.md generator that uses emojis and markdown.\"},"
                + "{\"role\":\"user\",\"content\":" + escapeJson(prompt) + "}"
                + "],"
                + "\"max_tokens\":2048,"
                + "\"temperature\":0.7"
                + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        int status = conn.getResponseCode();
        InputStream in = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        if (status != 200) {
            throw new IOException("Groq API error " + status + ": " + response);
        }

        String json = response.toString();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray choices = jsonObject.getJSONArray("choices");
        String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
        return content;
    }

    private String buildPrompt(String codeSummary, String projectType) {
        return "You are an expert AI that writes long and detailed GitHub README.md files using markdown and emojis. " +
                "Create a well-structured, polished README for a " + projectType + " project. " +
                "The README should be long enough to span at least two printed pages. Include rich explanations and examples in each section.\n\n" +
                "Sections to include:\n" +
                "1. 🚀 Project Title & Tagline\n" +
                "2. 📖 Description (2–3 paragraphs)\n" +
                "3. ✨ Features (minimum 8 points)\n" +
                "4. 🧰 Tech Stack Table (frontend, backend, tools)\n" +
                "5. 📁 Project Structure (with brief explanation for each folder)\n" +
                "6. ⚙️ How to Run (setup, environment, build, deploy)\n" +
                "7. 🧪 Testing Instructions\n" +
                "8. 📸 Screenshots (use placeholders if needed)\n" +
                "9. 📦 API Reference (if applicable)\n" +
                "10. 👤 Author\n" +
                "11. 📝 License\n\n" +
                "Here is the project code summary:\n" + codeSummary;
    }

    private String summarizeCode(String path) throws IOException {
        StringBuilder summary = new StringBuilder("## Project File Summary:\n");

        Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .filter(file -> {
                    String name = file.getFileName().toString().toLowerCase();
                    return name.endsWith(".java") || name.endsWith(".js") || name.endsWith(".ts")
                            || name.endsWith(".py") || name.endsWith(".html")
                            || name.endsWith(".css") || name.equals("pom.xml")
                            || name.equals("package.json") || name.equals("index.js");
                })
                .limit(15)  // ✅ Limit number of files
                .forEach(file -> {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        int maxLines = Math.min(8, lines.size());  // ✅ Limit lines per file

                        summary.append("\n---\n")
                                .append("**File:** ").append(file.getFileName()).append("\n")
                                .append("```").append(getLanguage(file.getFileName().toString())).append("\n");

                        for (int i = 0; i < maxLines; i++) {
                            String line = lines.get(i);
                            if (line.length() > 100) line = line.substring(0, 100); // ✅ Shorten long lines
                            summary.append(line).append("\n");
                        }

                        summary.append("```\n");

                    } catch (IOException ignored) {}
                });

        return summary.toString();
    }

    private String detectProjectType(String path) throws IOException {
        boolean hasPom = Files.walk(Paths.get(path)).anyMatch(p -> p.getFileName().toString().equals("pom.xml"));
        boolean hasPackageJson = Files.walk(Paths.get(path)).anyMatch(p -> p.getFileName().toString().equals("package.json"));
        boolean hasHtml = Files.walk(Paths.get(path)).anyMatch(p -> p.toString().endsWith(".html"));
        boolean hasPython = Files.walk(Paths.get(path)).anyMatch(p -> p.toString().endsWith(".py"));

        if (hasPom) return "Java Spring Boot (Maven)";
        if (hasPackageJson) {
            String content = Files.readString(Paths.get(path, "package.json"));
            return content.contains("react") ? "React (JavaScript)" : "Node.js";
        }
        if (hasPython) return "Python Project";
        if (hasHtml) return "Static Website (HTML/CSS/JS)";
        return "Generic Codebase";
    }

    private String getLanguage(String fileName) {
        if (fileName.endsWith(".java")) return "java";
        if (fileName.endsWith(".js") || fileName.equals("package.json")) return "javascript";
        if (fileName.endsWith(".ts")) return "typescript";
        if (fileName.endsWith(".html")) return "html";
        if (fileName.endsWith(".css")) return "css";
        if (fileName.endsWith(".py")) return "python";
        if (fileName.equals("pom.xml")) return "xml";
        return "";
    }

    private String escapeJson(String text) {
        String sanitized = text.replaceAll("[\\x00-\\x1F&&[^\\n\\r\\t]]", "");
        return "\"" + sanitized
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t") + "\"";
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private String injectEmojis(String markdown) {
        return markdown
                .replaceAll("(?i)^##\\s*features", "## ✨ Features")
                .replaceAll("(?i)^##\\s*project structure", "## 📁 Project Structure")
                .replaceAll("(?i)^##\\s*how to run", "## ⚙️ How to Run")
                .replaceAll("(?i)^##\\s*tech stack", "## 🧰 Tech Stack")
                .replaceAll("(?i)^##\\s*license", "## 📝 License")
                .replaceAll("(?i)^##\\s*author", "## 👤 Author")
                .replaceAll("(?i)^##\\s*description", "## 📖 Description");
    }

    public String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}

