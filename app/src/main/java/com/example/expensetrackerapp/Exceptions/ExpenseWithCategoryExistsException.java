package com.example.expensetrackerapp.Exceptions;

public class ExpenseWithCategoryExistsException extends RuntimeException {
    public ExpenseWithCategoryExistsException(String message) {
        super(message);
    }
}
