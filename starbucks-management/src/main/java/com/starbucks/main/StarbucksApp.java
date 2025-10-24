package com.starbucks.main;

import java.io.IOException;

import com.starbucks.model.Employee;
import com.starbucks.service.CustomerService;
import com.starbucks.service.EmployeeService;
import com.starbucks.service.MenuService;
import com.starbucks.service.OrderService;
import com.starbucks.service.ProductService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StarbucksApp extends Application {

    // THÊM: Khắc phục lỗi 1 - Khai báo biến static cho session (Nhân viên đăng nhập)
    private static Employee currentLoggedInEmployee; 
    
    // Khởi tạo các service (Giữ nguyên)
    private final EmployeeService employeeService = new EmployeeService();
    private final ProductService productService = new ProductService(); 
    private final MenuService menuService = new MenuService(productService);
    
    private final CustomerService customerService = new CustomerService();
    private final OrderService orderService = new OrderService(menuService, customerService); 
    
    private static StarbucksApp instance;

    public StarbucksApp() {
        instance = this;
    }

    public static StarbucksApp getInstance() {
        return instance;
    }

    // Setter cho session (Đã sửa lỗi khai báo biến)
    public static void setCurrentLoggedInEmployee(Employee employee) {
        currentLoggedInEmployee = employee;
    }
    
    // Getter cho session (ĐÃ SỬA LỖI 2 - Trả về biến lưu trữ)
    public static Employee getCurrentLoggedInEmployee() {
        return currentLoggedInEmployee;
    }

    public EmployeeService getEmployeeService() { return employeeService; }
    public MenuService getMenuService() { return menuService; }
    public CustomerService getCustomerService() { return customerService; }
    public OrderService getOrderService() { return orderService; }
    public ProductService getProductService() { return productService; }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Tải màn hình Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starbucks/view/LoginView.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Starbucks Management System - Login");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Hàm tiện ích để chuyển đổi Scene trên cùng một Stage (Giữ nguyên)
     */
    public void switchScene(Stage stage, String fxmlPath, String title) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ LỖI NGHIÊM TRỌNG: KHÔNG TÌM THẤY FXML.");
                throw new IOException("FXML resource not found: " + fxmlPath);
            }
            
            FXMLLoader loader = new FXMLLoader(resource); 
            Parent root = loader.load();

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Thông báo lỗi cho người dùng
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Lỗi Tải Giao Diện (Runtime)");
            alert.setHeaderText("Không thể chuyển đổi màn hình.");
            alert.setContentText("Chi tiết lỗi: " + e.getMessage());
            alert.showAndWait();
        }
    }
}