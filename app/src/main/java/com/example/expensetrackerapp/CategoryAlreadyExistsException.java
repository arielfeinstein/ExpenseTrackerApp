package com.example.expensetrackerapp;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String categoryName) {
      super(categoryName + " already exists");
    }
}
