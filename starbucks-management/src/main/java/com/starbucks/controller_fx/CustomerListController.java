package com.starbucks.controller_fx;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.starbucks.main.StarbucksApp;
import com.starbucks.model.Customer;
import com.starbucks.model.Order;
import com.starbucks.model.OrderDetail;
import com.starbucks.service.CustomerService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CustomerListController {

    // == FXML COMPONENTS ==
    @FXML private TextField searchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameCol;
    // ĐÃ XÓA: @FXML private TableColumn<Customer, String> phoneCol; 
    @FXML private TableColumn<Customer, String> lastOrderCol;
    @FXML private TableColumn<Customer, Integer> pointsCol;
    @FXML private Label customerDetailLabel;
    @FXML private TableView<Order> historyTable; 
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, Integer> orderTotalCol;
    @FXML private TableColumn<Order, String> orderItemsCol;
    
    // Services
    private final CustomerService customerService = StarbucksApp.getInstance().getCustomerService();

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Cấu hình TableColumn cho Customer Table
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        // ĐÃ XÓA: phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("rewardPoints"));
        // Yêu cầu Customer Model có getter getLastOrderDateString()
        lastOrderCol.setCellValueFactory(new PropertyValueFactory<>("lastOrderDateString")); 

        customerTable.setItems(customerList);

        // Cấu hình TableColumn cho History Table
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDateString")); 
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        // Cột Order Items: Sử dụng Custom Cell Value Factory để hiển thị list OrderDetail
        orderItemsCol.setCellValueFactory(cellData -> {
            // Lấy danh sách items từ Order và tạo chuỗi tóm tắt
            String itemsSummary = cellData.getValue().getItems().stream()
                .map(OrderDetail::getProductName)
                .collect(Collectors.joining(", "));
            // Trả về một SimpleStringProperty
            return new javafx.beans.property.SimpleStringProperty(itemsSummary);
        });

        // Tải toàn bộ danh sách khách hàng ban đầu
        loadCustomers("");

        // Listener để hiển thị chi tiết khách hàng khi chọn
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showCustomerDetails(newSelection);
            }
        });
    }

    /**
     * Tải danh sách khách hàng từ database dựa trên từ khóa tìm kiếm.
     */
    private void loadCustomers(String keyword) {
        
        List<Customer> customers = customerService.getAllCustomers(); 
        
        // Logic tìm kiếm (Đã bỏ tìm kiếm theo phone number)
        if (keyword != null && !keyword.trim().isEmpty()) {
            customers = customers.stream()
                .filter(c -> c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
        }

        customerList.clear();
        customerList.addAll(customers);
        System.out.println("LOG: Đã tải " + customerList.size() + " khách hàng.");
    }

    /**
     * Xử lý tìm kiếm khi người dùng nhấn nút hoặc Enter.
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        loadCustomers(keyword);
    }
    
    /**
     * Hiển thị chi tiết khách hàng và lịch sử mua hàng.
     */
    private void showCustomerDetails(Customer customer) {
        customerDetailLabel.setText(
            "Tên: " + customer.getName() + "\n" +
            "ID: " + customer.getCustomerId() + "\n" + // ĐÃ BỎ SỐ ĐT
            "Điểm Thưởng: " + customer.getRewardPoints() + " điểm"
        );

        // Tải lịch sử giao dịch từ CustomerService
        List<Order> history = customerService.getOrderHistoryByCustomerId(customer.getCustomerId());
        historyTable.setItems(FXCollections.observableArrayList(history));
    }

    /**
     * Quay lại màn hình Main Menu.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(StarbucksApp.class.getResource("/com/starbucks/view/MainMenuView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Starbucks Main Menu");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Không thể quay lại Menu.").showAndWait();
        }
    }
}
