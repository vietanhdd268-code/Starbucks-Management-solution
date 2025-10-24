package com.starbucks.model;

/**
 * Model đại diện cho một món hàng cụ thể trong đơn hàng (Line Item).
 * Được sử dụng khi đọc Lịch sử giao dịch từ Database.
 */
public class OrderDetail {
    private String detailId;
    private String orderId;
    private String productId;
    private String productName;
    private int quantity;
    private int priceAtSale;

    // Constructor đầy đủ
    public OrderDetail(String detailId, String orderId, String productId, String productName, int quantity, int priceAtSale) {
        this.detailId = detailId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
    }

    // Constructor đơn giản
    public OrderDetail(String productId, String productName, int quantity, int priceAtSale) {
        this(null, null, productId, productName, quantity, priceAtSale);
    }
    
    // --- GETTERS ---
    public String getDetailId() { return detailId; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public int getPriceAtSale() { return priceAtSale; }
    
    // --- SETTERS (BẮT BUỘC cho OrderService khi tạo ID) ---
    public void setDetailId(String detailId) { this.detailId = detailId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPriceAtSale(int priceAtSale) { this.priceAtSale = priceAtSale; }
}
