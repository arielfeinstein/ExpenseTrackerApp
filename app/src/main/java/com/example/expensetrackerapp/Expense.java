package com.example.expensetrackerapp;

import java.util.Date;

public class Expense implements Comparable<Expense> {
    private String id;
    private double amount;
    private Category category;
    private Date transactionDate;
    private String description;

    // No-arg constructor for Firestore
    public Expense() {}

    // Constructor
    public Expense(double amount, Category category, Date transactionDate, String description) {
        this.amount = amount;
        this.category = category;
        this.transactionDate = transactionDate;
        this.description = description;
        this.id=null;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Date getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // toString

    @Override
    public String toString() {
        return "Expense{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", category=" + category +
                ", transactionDate=" + transactionDate +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public int compareTo(Expense expense) {
        return this.transactionDate.compareTo(expense.transactionDate);
    }
}

