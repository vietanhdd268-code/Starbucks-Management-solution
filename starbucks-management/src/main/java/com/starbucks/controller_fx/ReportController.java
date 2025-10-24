package com.starbucks.controller_fx;

import java.util.Map;

import com.starbucks.main.StarbucksApp;
import com.starbucks.model.Employee;
import com.starbucks.model.Product;
import com.starbucks.service.MenuService;
import com.starbucks.service.OrderService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ReportController {

    // Khai báo các FXML Components từ ReportView.fxml
    @FXML private Label dailyTotalLabel; // Tổng doanh thu
    @FXML private TableView<ReportItem> productSalesTable;
    @FXML private TableColumn<ReportItem, String> productNameCol;
    @FXML private TableColumn<ReportItem, Integer> quantityCol;
    @FXML private TableColumn<ReportItem, Integer> totalSalesCol;
    
    private final OrderService orderService = StarbucksApp.getInstance().getOrderService();
    private final MenuService menuService = StarbucksApp.getInstance().getMenuService();
    
    private ObservableList<ReportItem> reportList = FXCollections.observableArrayList();
    private int currentTotalRevenue = 0;

    @FXML
    public void initialize() {
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalSalesCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        productSalesTable.setItems(reportList);
        
        loadSalesData();
    }
    
    // Tải dữ liệu thống kê từ OrderService
    private void loadSalesData() {
        reportList.clear();
        Map<String, int[]> stats = orderService.getSalesStatistics(); // Hàm đã thêm vào OrderService
        currentTotalRevenue = 0;
        
        for (Map.Entry<String, int[]> entry : stats.entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue()[0];
            int total = entry.getValue()[1];
            
            Product product = menuService.getProductById(productId);
            String productName = (product != null) ? product.getButtonLabel() : "ID: " + productId;
            
            reportList.add(new ReportItem(productName, quantity, total));
            currentTotalRevenue += total;
        }

        dailyTotalLabel.setText(String.format("%,d", currentTotalRevenue) + " VND");
    }

    /** Xử lý nút TỔNG KẾT (Chốt sổ EOD Logic) */
    @FXML
    private void handleFinalizeDailyReport() {
        if (currentTotalRevenue == 0) {
            new Alert(AlertType.WARNING, "Không có giao dịch nào để tổng kết.", ButtonType.OK).showAndWait();
            return;
        }
        
        // LẤY THÔNG TIN NHÂN VIÊN ĐANG ĐĂNG NHẬP
        Employee employee = StarbucksApp.getCurrentLoggedInEmployee();
        String employeeId = (employee != null) ? employee.getId() : "E000";

        Alert confirmation = new Alert(AlertType.CONFIRMATION, 
            "Bạn có chắc chắn muốn chốt sổ và lưu Báo cáo Cuối ngày (EOD) với tổng doanh thu " + 
            String.format("%,d", currentTotalRevenue) + " VND?", 
            ButtonType.YES, ButtonType.NO);
            
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // SỬA LỖI: Gọi hàm với employeeId
                if (orderService.finalizeDailyReport(currentTotalRevenue, employeeId)) { 
                    new Alert(AlertType.INFORMATION, "✅ Chốt sổ (EOD) thành công! Người chốt: " + employeeId, ButtonType.OK).showAndWait();
                    loadSalesData();
                } else {
                    new Alert(AlertType.ERROR, "❌ Lỗi: Không thể hoàn tất quy trình chốt sổ. Vui lòng kiểm tra kết nối DB.", ButtonType.OK).showAndWait();
                }
            }
        });
    }

    /** Quay lại Menu Chính */
    @FXML
    private void handleBackToMenu() {
        Stage stage = (Stage) dailyTotalLabel.getScene().getWindow();
        StarbucksApp.getInstance().switchScene(stage, 
                                               "/com/starbucks/view/MainMenuView.fxml", 
                                               "Starbucks POS Terminal");
    }
    
    // Lớp nội bộ để hiển thị dữ liệu thống kê trong TableView
    public static class ReportItem {
        private final String productName;
        private final int quantity;
        private final int totalPrice;

        public ReportItem(String productName, int quantity, int totalPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }
        
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public int getTotalPrice() { return totalPrice; }
    }
}