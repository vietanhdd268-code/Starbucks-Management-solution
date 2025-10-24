package com.starbucks.model;

import java.time.LocalDateTime;

public class Customer {
    private String customerId;
    private String name;
    // ĐÃ LOẠI BỎ: private String phoneNumber;
    private int rewardPoints;
    private LocalDateTime lastOrderDate;

    // Constructor MỚI: BỎ phoneNumber
    public Customer(String customerId, String name, int rewardPoints, LocalDateTime lastOrderDate) {
        this.customerId = customerId;
        this.name = name;
        this.rewardPoints = rewardPoints;
        this.lastOrderDate = lastOrderDate;
    }

    // Getters
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    // Bỏ Getter getPhoneNumber()
    public int getRewardPoints() { return rewardPoints; }
    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    
    public String getLastOrderDateString() { 
        return (lastOrderDate != null) ? lastOrderDate.toLocalDate().toString() : "N/A";
    }
}
