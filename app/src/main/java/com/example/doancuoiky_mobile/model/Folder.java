package com.example.doancuoiky_mobile.model;

import java.util.List;

public class Folder {
    private String name;
    private String description;
    private List<Topic> topics;  // Danh sách các Topic trong Folder

    public Folder() {
        // Cần thiết cho việc giải mã của Firestore
    }

    // Constructor
    public Folder(String name, String description, List<Topic> topics) {
        this.name = name;
        this.description = description;
        this.topics = topics;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}
