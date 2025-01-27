package com.example.expensetrackerapp;

public class ExpenseWithCategoryExistsException extends RuntimeException {
    public ExpenseWithCategoryExistsException(String message) {
        super(message);
    }
}
