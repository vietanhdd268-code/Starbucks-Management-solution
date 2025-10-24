package com.starbucks.controller_fx;

import java.util.Optional;

import com.starbucks.main.StarbucksApp;
import com.starbucks.model.Employee;
import com.starbucks.service.EmployeeService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginControllerFx {
    
    @FXML private TextField staffIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private EmployeeService employeeService;

    @FXML
    public void initialize() {
        try {
            
            employeeService = StarbucksApp.getInstance().getEmployeeService();
        } catch (Exception e) {
            messageLabel.setText("Lỗi khởi tạo Service: " + e.getMessage());
            System.err.println("❌ LỖI KHỞI TẠO EmployeeService trong LoginController.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        if (employeeService == null) {
            messageLabel.setText("Lỗi hệ thống: Dịch vụ đăng nhập chưa sẵn sàng.");
            return;
        }
        
        String staffId = staffIdField.getText();
        String password = passwordField.getText();

        Employee employee = employeeService.login(staffId, password);

    if (employee != null) {
        messageLabel.setText("Đăng nhập thành công!"); 

        StarbucksApp.setCurrentLoggedInEmployee(employee);

        Stage stage = (Stage) staffIdField.getScene().getWindow();

        try {
            // Tải FXML
            FXMLLoader fxmlLoader = new FXMLLoader(StarbucksApp.class.getResource("/com/starbucks/view/MainMenuView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            MainMenuController controller = fxmlLoader.getController(); 
            controller.setLoggedInEmployee(employee); 
            
            stage.setTitle("Starbucks POS Terminal - Welcome, " + employee.getName() + "!");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            messageLabel.setText("Lỗi: Không thể tải màn hình Menu chính.");
            e.printStackTrace();
        }

        } else {
            messageLabel.setText("Lỗi: Staff ID hoặc Mật khẩu không đúng \nStaff ID or Password is incorrect.");
        }
    }

    // ----------------------------------------------------------------------
    // CHANGE PASSWORD (Yêu cầu ID xác nhận SM/IC)
    // ----------------------------------------------------------------------
    @FXML
    private void handleChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi Mật khẩu");
        dialog.setHeaderText("Cần xác nhận từ SM/IC để đổi mật khẩu.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField idField = new TextField();
        idField.setPromptText("Staff ID Cần Đổi");
        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Mật khẩu CŨ");
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Mật khẩu MỚI");
        
        // Staff ID xác nhận (SM/IC)
        TextField authIdField = new TextField(); 
        authIdField.setPromptText("Staff ID SM/IC Xác nhận");

        grid.add(new Label("Staff ID Cần Đổi:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Mật khẩu CŨ:"), 0, 1);
        grid.add(oldPassField, 1, 1);
        grid.add(new Label("Mật khẩu MỚI:"), 0, 2);
        grid.add(newPassField, 1, 2);
        grid.add(new Label("ID SM/IC Xác nhận:"), 0, 3);
        grid.add(authIdField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String staffId = idField.getText();
            String oldPassword = oldPassField.getText();
            String newPassword = newPassField.getText();
            String authId = authIdField.getText(); 

            if (staffId.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || authId.isEmpty()) {
                messageLabel.setText("Lỗi: Không được để trống bất kỳ trường nào.");
                return;
            }
            
            // BƯỚC XÁC THỰC: KIỂM TRA ID SM/IC
            if (!employeeService.isAuthorizedManager(authId)) {
                messageLabel.setText("Lỗi: ID Xác nhận (" + authId + ") không phải là SM/IC hoặc không tồn tại.");
                return;
            }

            if (employeeService.changePassword(staffId, oldPassword, newPassword)) {
                messageLabel.setText("Thông báo: Đổi mật khẩu thành công!");
            } else {
                messageLabel.setText("Lỗi: Staff ID cần đổi hoặc Mật khẩu cũ không chính xác.");
            }
        }
    }
    
    // ----------------------------------------------------------------------
    // RESET PASSWORD (Mật khẩu về "1", yêu cầu ID xác nhận SM/IC)
    // ----------------------------------------------------------------------
    @FXML
    private void handleResetPassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đặt lại Mật khẩu");
        dialog.setHeaderText("Mật khẩu sẽ được đặt lại thành '1'. Cần xác nhận từ SM/IC.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField idField = new TextField();
        idField.setPromptText("Staff ID Cần Reset");
        
        // Staff ID xác nhận (SM/IC)
        TextField authIdField = new TextField(); 
        authIdField.setPromptText("Staff ID SM/IC Xác nhận");

        grid.add(new Label("Staff ID Cần Reset:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("ID SM/IC Xác nhận:"), 0, 1);
        grid.add(authIdField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String staffId = idField.getText();
            String authId = authIdField.getText(); 

            if (staffId.isEmpty() || authId.isEmpty()) {
                messageLabel.setText("Lỗi: Vui lòng nhập Staff ID và ID SM/IC xác nhận.");
                return;
            }
            
            // BƯỚC XÁC THỰC: KIỂM TRA ID SM/IC
            if (!employeeService.isAuthorizedManager(authId)) {
                messageLabel.setText("Lỗi: ID Xác nhận (" + authId + ") không phải là SM/IC hoặc không tồn tại.");
                return;
            }

            if (employeeService.resetPassword(staffId)) {
                messageLabel.setText("Thông báo: Reset mật khẩu thành công! Mật khẩu mới: 1");
            } else {
                messageLabel.setText("Lỗi: Không tìm thấy Staff ID cần reset này.");
            }
        }
    }
}
