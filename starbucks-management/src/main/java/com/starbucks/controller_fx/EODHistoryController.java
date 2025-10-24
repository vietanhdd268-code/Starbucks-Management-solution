package com.starbucks.controller_fx;

import java.util.List;

import com.starbucks.main.StarbucksApp;
import com.starbucks.model.DailyReport; // Import Model mới
import com.starbucks.service.OrderService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class EODHistoryController {

    @FXML private TableView<DailyReport> eodHistoryTable; // Sử dụng Model DailyReport
    @FXML private TableColumn<DailyReport, String> reportIdCol;
    @FXML private TableColumn<DailyReport, String> reportDateCol;
    @FXML private TableColumn<DailyReport, Integer> totalRevenueCol;
    @FXML private TableColumn<DailyReport, String> employeeNameCol;
    
    private final OrderService orderService = StarbucksApp.getInstance().getOrderService();
    
    private ObservableList<DailyReport> reportList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Cấu hình các cột để khớp với các Getters trong DailyReport.java
        reportIdCol.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        // reportDateCol cần getReportDateString() từ Model
        reportDateCol.setCellValueFactory(new PropertyValueFactory<>("reportDateString")); 
        totalRevenueCol.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        employeeNameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        
        eodHistoryTable.setItems(reportList);
        
        loadEODHistory();
    }
    
    private void loadEODHistory() {
        reportList.clear();
        try {
            List<DailyReport> history = orderService.getEODHistory();
            reportList.addAll(history);
            System.out.println("LOG: Đã tải " + history.size() + " báo cáo EOD.");
        } catch (Exception e) {
            System.err.println("Lỗi khi tải lịch sử EOD: " + e.getMessage());
        }
    }

    /**
     * Quay lại màn hình Main Menu.
     */
    @FXML
    private void handleBackToMenu() {
        // ... (Logic quay lại giữ nguyên) ...
        Stage stage = (Stage) eodHistoryTable.getScene().getWindow(); 
        StarbucksApp.getInstance().switchScene(stage, 
                                               "/com/starbucks/view/MainMenuView.fxml", 
                                               "Starbucks POS Terminal");
    }
}