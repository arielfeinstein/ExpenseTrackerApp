package com.example.expensetrackerapp;

import androidx.annotation.Nullable;
import java.util.Objects;
import androidx.annotation.NonNull;

public class Category {
    private String id;
    private String name;
    private int imgIndexInsideArraysXml; // 0 for first img resource, 1 for second img resource and so on...

    // No-arg constructor for FireStore
    public Category() {
    }

    // Constructor
    public Category(String name, int imgIndexInsideArraysXml) {
        this.id = null;
        this.name = name;
        this.imgIndexInsideArraysXml = imgIndexInsideArraysXml;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getImgIndexInsideArraysXml() { return imgIndexInsideArraysXml; }
    public void setImageResourceId(int imgIndexInsideArraysXml) { this.imgIndexInsideArraysXml = imgIndexInsideArraysXml; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    // toString
    @NonNull
    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imgIndexInsideArraysXml=" + imgIndexInsideArraysXml +
                '}';
    }
}
