package com.starbucks.model;

/**
 * Lớp model đại diện cho một sản phẩm.
 * Sử dụng cấu trúc khớp với Database SQL của bạn (productId, name, size, temp, price).
 */
public class Product {
    private String productId;
    private String name;
    private String size;
    private String temp;
    private int price;

    // Constructor
    public Product(String productId, String name, String size, String temp, int price) {
        this.productId = productId;
        this.name = name;
        this.size = size;
        this.temp = temp;
        this.price = price;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName() { 

        return name; 
    }
    public String getSize() { return size; }
    public String getTemp() { return temp; }

    public int getPrice() { return price; } 

    /**
     * Phương thức tiện ích để hiển thị trên nút (Ví dụ: TALL Iced Latte)
     */
    public String getButtonLabel() {
        // Kết hợp size và name, viết hoa size
        return size.toUpperCase() + " " + name;
    }
    
    // Phương thức tiện ích để tạo key tra cứu
    public String getLookupKey() {
        return name + " - " + size + " - " + temp; 
    }
    
    // Setters
    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setSize(String size) { this.size = size; }
    public void setTemp(String temp) { this.temp = temp; }
    public void setPrice(int price) { this.price = price; }
}
