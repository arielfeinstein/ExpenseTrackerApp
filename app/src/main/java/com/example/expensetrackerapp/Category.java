package com.example.expensetrackerapp;

public class Category {
    private String id;
    private String name;
    private int imageResourceId;

    // No-arg constructor for Firestore
    public Category() {
    }

    // Constructor
    public Category(String name, int imageResourceId) {
        this.id = null;
        this.name = name;
        this.imageResourceId = imageResourceId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getImageResourceId() { return imageResourceId; }
    public void setImageResourceId(int imageResourceId) { this.imageResourceId = imageResourceId; }

    // toString
    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageResourceId=" + imageResourceId +
                '}';
    }
}
