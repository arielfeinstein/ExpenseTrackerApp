package com.example.expensetrackerapp.Exceptions;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String categoryName) {
      super(categoryName + " already exists");
    }
}
