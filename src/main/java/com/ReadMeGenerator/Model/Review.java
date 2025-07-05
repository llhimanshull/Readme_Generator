package com.ReadMeGenerator.Model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String name;
    private String review;

    public Review() {
    }

    public Review(String name, String reivew) {
        this.name = name;
        this.review = reivew;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String reivew) {
        this.review = reivew;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", reivew='" + review + '\'' +
                '}';
    }
}
