package com.starbucks.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private String customerName;
    private int totalAmount;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private List<OrderDetail> items;

    // Constructor Mặc định 
    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
    }
    
    // Constructor đầy đủ 
    public Order(String orderId, String customerId, String customerName, int totalAmount, String paymentMethod, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.orderDate = orderDate;
        this.items = new ArrayList<>();
    }

    // Constructor cho tạo order mới 
    public Order(String customerId, String customerName, int totalAmount, String paymentMethod, List<OrderDetail> items) {
        this();
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    // --- GETTERS ---
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public int getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public List<OrderDetail> getItems() { return items; }
    
    // Getter BẮT BUỘC cho TableView
    public String getOrderDateString() { 
        return (orderDate != null) ? orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"; 
    }

    // --- SETTERS  ---
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public void setItems(List<OrderDetail> items) { this.items = items; }
}