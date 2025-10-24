package com.starbucks.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.starbucks.db.DbConnector; 
import com.starbucks.model.Product;

public class ProductService {

    private final String TABLE_NAME = DbConnector.getProductTableName(); 

    /**
     * Tải tất cả sản phẩm từ database. Được gọi bởi MenuService khi khởi động.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, size, temp, price FROM " + TABLE_NAME + " ORDER BY temp, name, size";

        try (Connection conn = DbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                        rs.getString("product_id"),
                        rs.getString("name"),
                        rs.getString("size"),
                        rs.getString("temp"),
                        rs.getInt("price")
                    );
                    products.add(product);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tải tất cả sản phẩm: " + e.getMessage());
        }
        return products;
    }
    
    /**
     * Phương thức này KHÔNG CÒN được sử dụng trực tiếp bởi Controller
     * mà chỉ được giữ lại cho mục đích tương thích nếu cần.
     * Logic chính đã được chuyển sang MenuService.
     */
    @Deprecated 
    public List<Product> getProductsByTemp(String temp) {
        // ... (Nội dung cũ của hàm này sẽ không còn được gọi) ...
        return new ArrayList<>(); 
    }
    
    public Product getProductById(String productId) {
        // Logic truy vấn DB trực tiếp nếu cần tra cứu một sản phẩm cụ thể
        return null; 
    }
}