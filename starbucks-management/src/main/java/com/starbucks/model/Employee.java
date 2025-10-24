package com.starbucks.model;

public class Employee {
    private String id;
    private String name;
    private String password;
    private String role; 

    public Employee(String id, String name, String password, String role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setPassword(String pasword) {
        this.password = pasword;
    }

    // Setters (Nếu cần)
    // public void setPassword(String password) { this.password = password; }
}