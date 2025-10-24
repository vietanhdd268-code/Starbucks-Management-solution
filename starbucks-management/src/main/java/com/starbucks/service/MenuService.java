package com.starbucks.service;

import com.starbucks.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MenuService hoạt động như một lớp Menu Repository và Cache.
 * Nó chịu trách nhiệm tải tất cả sản phẩm từ DB khi khởi động 
 * và cung cấp chúng theo danh mục (Nhiệt độ) cho UI.
 */
public class MenuService {
    
    // Cache: Map lưu trữ danh sách sản phẩm theo nhiệt độ (HOT, ICE)
    private Map<String, List<Product>> menuCache;
    private ProductService productService;

    /**
     * Khởi tạo MenuService, nhận ProductService làm dependency để tải dữ liệu.
     */
    public MenuService(ProductService productService) {
        this.productService = productService;
        this.menuCache = new HashMap<>();
        loadInitialMenu(); // Tải dữ liệu lần đầu
    }

    /**
     * Tải tất cả sản phẩm từ ProductService và sắp xếp chúng vào menuCache theo trường 'temp'.
     */
    public void loadInitialMenu() {
        System.out.println("LOG: Đang tải và caching menu...");
        try {
            // Lấy tất cả sản phẩm (Bạn có thể cần thêm một hàm getAllProducts() trong ProductService)
            List<Product> allProducts = productService.getAllProducts(); 
            
            // Nhóm sản phẩm theo nhiệt độ (temp)
            menuCache = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getTemp));

            System.out.println("LOG: Caching menu hoàn tất. Các danh mục: " + menuCache.keySet());
            
        } catch (Exception e) {
            System.err.println("LỖI: Không thể tải menu ban đầu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy danh sách sản phẩm theo nhiệt độ (temp) từ cache.
     * @param temp "HOT" hoặc "ICE" (hoặc bất kỳ temp nào khác trong DB).
     * @return List<Product>
     */
    public List<Product> getProductsByTemp(String temp) {
        // Áp dụng logic chuyển đổi UI (ICED) -> DB (ICE) tại đây hoặc trong Controller/ProductService.
        // Tốt nhất là giữ nguyên logic chuẩn hóa trong ProductService nếu nó còn được sử dụng.
        String key = temp.equals("ICED") ? "ICE" : temp;
        return menuCache.getOrDefault(key, new ArrayList<>());
    }
    
    /**
     * Lấy một sản phẩm theo ID (Tùy chọn, cần duyệt qua cache).
     */
    public Product getProductById(String productId) {
         for (List<Product> products : menuCache.values()) {
            for (Product product : products) {
                if (product.getProductId().equals(productId)) {
                    return product;
                }
            }
        }
        return null;
    }
}