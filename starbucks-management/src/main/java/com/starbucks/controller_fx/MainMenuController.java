package com.starbucks.controller_fx;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.starbucks.main.StarbucksApp;
import com.starbucks.model.Employee;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenuController {

    @FXML private Button exitButton; 
    @FXML private Label employeeNameLabel; 

    // KHAI BÁO MỚI: Khắc phục lỗi đỏ (Báo lỗi nếu thiếu)
    @FXML private Label dateTimeLabel; 

    private Employee loggedInEmployee;

    @FXML
    public void initialize() {
        startClock(); // Khởi động đồng hồ thời gian thực
    }
    
    // PHƯƠNG THỨC MỚI: Khởi động đồng hồ thời gian thực
    private void startClock() {
        // Định dạng Ngày/Tháng/Năm Giờ:Phút:Giây
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"); 
        
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    
    public void setLoggedInEmployee(Employee employee) {
        this.loggedInEmployee = employee;
        updateEmployeeName(); 
    }

    private void updateEmployeeName() {
        if (loggedInEmployee != null && employeeNameLabel != null) {
            employeeNameLabel.setText(loggedInEmployee.getName() + " (" + loggedInEmployee.getId() + ")");
        }
    }

    public Employee getLoggedInEmployee() { 
    return loggedInEmployee; 
}
    @FXML
    private void handleOpenPOS() {
        // Logic mở POS giữ nguyên
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(StarbucksApp.class.getResource("/com/starbucks/view/POSView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage posStage = new Stage();
            posStage.setTitle("Starbucks POS Terminal");
            posStage.setScene(scene);
            posStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi khi mở POS.").showAndWait();
        }
    }
    
    // Xử lý nút CMS / Thống kê
    @FXML
    private void handleOpenReport() {
        StarbucksApp app = StarbucksApp.getInstance();
        Stage stage = (Stage) dateTimeLabel.getScene().getWindow(); // Sử dụng dateTimeLabel để lấy Stage

        try {
            app.switchScene(stage, "/com/starbucks/view/ReportView.fxml", "Starbucks - Thống Kê Doanh Thu");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi khi mở màn hình Thống kê Doanh thu.").showAndWait();
            e.printStackTrace();
        }
    }

    // Xử lý nút EOD History (Lịch sử Chốt sổ)
    @FXML
    private void handleOpenEODHistory() {
        StarbucksApp app = StarbucksApp.getInstance();
        Stage stage = (Stage) dateTimeLabel.getScene().getWindow();

        try {
            app.switchScene(stage, "/com/starbucks/view/EODHistoryView.fxml", "Starbucks - Lịch Sử Chốt Sổ EOD");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi khi mở màn hình Lịch sử EOD.").showAndWait();
            e.printStackTrace();
        }
    }
    

    // EXIT (Đăng xuất)
    @FXML
    private void handleExitButton() {
        Stage currentStage = (Stage) exitButton.getScene().getWindow();
        StarbucksApp.getInstance().switchScene(currentStage, 
                                            "/com/starbucks/view/LoginView.fxml", 
                                                "Starbucks Management System - Login");
        System.out.println("LOG: Đã thoát về màn hình đăng nhập.");
    }

    // handleCustomerClick
    @FXML
    private void handleCustomerClick() {
        StarbucksApp app = StarbucksApp.getInstance();
        Stage stage = (Stage) employeeNameLabel.getScene().getWindow();

        try {
            app.switchScene(stage, "/com/starbucks/view/CustomerView.fxml", "Starbucks - Quản Lý Khách Hàng");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi khi mở màn hình Khách hàng. Kiểm tra log.").showAndWait();
            e.printStackTrace();
        }
    }
}