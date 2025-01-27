package com.example.expensetrackerapp;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String categoryName) {
        super("Category " + categoryName + " not found");
    }
}
