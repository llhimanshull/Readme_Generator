package com.ReadMeGenerator.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Generated Count")
public class Count {
    @Id
    private String id;
    private int count = 0 ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Count() {
    }

    public Count(String id, int count) {
        this.id = id;
        this.count = count;
    }
}
