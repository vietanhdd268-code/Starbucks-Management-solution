package com.starbucks.controller_fx;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
 
import com.starbucks.main.StarbucksApp;
import com.starbucks.model.Order;
import com.starbucks.model.OrderDetail; // ĐÃ FIX LỖI IMPORT
import com.starbucks.model.Product; // ĐÃ FIX LỖI IMPORT
import com.starbucks.service.CustomerService;
import com.starbucks.service.MenuService;
import com.starbucks.service.OrderService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class POSControllerFx {

    // == FXML COMPONENTS ==
    @FXML private TableView<Product> orderTableView;
    @FXML private TableColumn<Product, String> itemCol;
    @FXML private TableColumn<Product, Integer> priceCol; 
    @FXML private Label totalLabel;
    @FXML private GridPane productGrid; 

    // == SERVICES & DATA ==
    private final MenuService menuService = StarbucksApp.getInstance().getMenuService(); 
    private final CustomerService customerService = StarbucksApp.getInstance().getCustomerService(); 
    private final OrderService orderService = StarbucksApp.getInstance().getOrderService(); 
    
    private String currentCategory = "HOT"; 
    private final ObservableList<Product> currentOrderItems = FXCollections.observableArrayList();
    // TRẠNG THÁI KHÁCH HÀNG
    private String customerNameForTransaction = "Khách vãng lai"; 
    private String customerIdForTransaction = null;
    
    private static final String[] DRINK_NAMES = {"Americano", "Latte", "Cappuchino", "Caramel Macchiato"};
    private static final String[] SIZES = {"TALL", "GRANDE", "VENTI"};


    // == KHỞI TẠO ==
    @FXML
    public void initialize() {
        System.out.println("LOG: Khởi tạo POS Controller...");
        
        itemCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price")); 

        orderTableView.setItems(currentOrderItems);
        
        loadProductsByTemp("HOT"); 
    }

    // == XỬ LÝ LƯỚI SẢN PHẨM ==

    /** Tải và hiển thị các nút sản phẩm dựa trên Nhiệt độ (Temp) đã chọn. */
    private void loadProductsByTemp(String temp) {
        productGrid.getChildren().clear(); 
        productGrid.getRowConstraints().clear(); 

        try {
            // ĐÃ THAY ĐỔI: Lấy sản phẩm từ MenuService (Cache)
            List<Product> products = menuService.getProductsByTemp(temp); 
            
            final int MAX_COLS = 4;
            int col = 0;
            int row = 0;

            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                
                productGrid.add(createProductButton(product), col, row);

                col++;
                if (col >= MAX_COLS) {
                    col = 0;
                    row++;
                }
            }
            
            int requiredRows = row + (col > 0 ? 1 : 0);
            if (requiredRows == 0) requiredRows = 1; 
            
            for (int i = 0; i < requiredRows; i++) {
                 javafx.scene.layout.RowConstraints rc = new javafx.scene.layout.RowConstraints();
                 rc.setVgrow(javafx.scene.layout.Priority.ALWAYS);
                 rc.setMinHeight(100); 
                 productGrid.getRowConstraints().add(rc);
            }

        } catch (Exception e) {
            System.err.println("LỖI: Không thể tải sản phẩm cho nhiệt độ " + temp);
            e.printStackTrace();
        }
    }

    /** Tạo một Button cho Sản phẩm. */
    private Button createProductButton(Product product) {
        Button button = new Button(product.getButtonLabel());
        button.setMaxWidth(Double.MAX_VALUE); 
        button.setMaxHeight(Double.MAX_VALUE); 
        
        button.getStyleClass().add("product-button"); 
        
        button.setOnAction(event -> handleProductSelect(product));
        return button;
    }

    /** Xử lý khi người dùng chọn một danh mục (HOT/ICED ESP). */
    @FXML
    private void handleCategorySelect(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String temp = (String) sourceButton.getUserData(); 
        
        if (!currentCategory.equals(temp)) {
            currentCategory = temp;
            loadProductsByTemp(currentCategory);
            System.out.println("LOG: Đã chọn nhiệt độ: " + temp);
        }
    }

    /** Xử lý khi người dùng chọn một sản phẩm. */
    private void handleProductSelect(Product product) {
        System.out.println("LOG: Đã thêm sản phẩm: " + product.getButtonLabel() + " - " + product.getPrice());
        currentOrderItems.add(product);
        updateTotal();
    }
    
    /** Cập nhật tổng tiền của đơn hàng. */
    private void updateTotal() {
        int total = currentOrderItems.stream() 
            .mapToInt(Product::getPrice)
            .sum();
        
        totalLabel.setText(String.format("%,d", total)); 
    }

    // == XỬ LÝ CHỨC NĂNG THAO TÁC ==
    
    /** Xử lý nút Clear All. */
    @FXML
    private void handleClearAll() {
        System.out.println("LOG: Xóa toàn bộ đơn hàng.");
        currentOrderItems.clear();
        updateTotal();
        customerNameForTransaction = "Khách vãng lai"; // Reset trạng thái khách hàng
        customerIdForTransaction = null; 
    }

    /** Xử lý nút Void (Xóa item đã chọn). */
    @FXML
    private void handleVoidItem() {
        Product selectedItem = orderTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            System.out.println("LOG: Xóa sản phẩm: " + selectedItem.getButtonLabel());
            currentOrderItems.remove(selectedItem);
            updateTotal();
        } else {
            new Alert(AlertType.WARNING, "Vui lòng chọn một sản phẩm để xóa (VOID).", ButtonType.OK).showAndWait();
        }
    }

    @FXML
    private void handlePayment() {
        if (currentOrderItems.isEmpty()) {
            new Alert(AlertType.WARNING, "Không có sản phẩm nào trong đơn hàng để thanh toán.", ButtonType.OK).showAndWait();
            return;
        }
        
        promptForCustomerName();
    }
    
    /**
     * Hiển thị hộp thoại yêu cầu Tên.
     */
    private void promptForCustomerName() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thông tin Khách hàng");
        dialog.setHeaderText("Vui lòng nhập Tên khách hàng (Bắt buộc)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Tên khách hàng");
        
        // CHỈ CÒN TRƯỜNG TÊN
        grid.add(new Label("Tên Khách hàng:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Focus vào trường tên
        nameField.requestFocus(); 
        
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String customerName = nameField.getText().trim();

            if (customerName.isEmpty()) {
                new Alert(AlertType.WARNING, "Tên khách hàng không được để trống.", ButtonType.OK).showAndWait();
                // Nếu tên trống, gọi lại hàm này để buộc nhập
                promptForCustomerName(); 
                return;
            }
            
            // CHỈ GỌI saveOrUpdateCustomer VỚI THAM SỐ TÊN
            boolean saveSuccess = customerService.saveOrUpdateCustomer(customerName);            
           
            if(saveSuccess) {
                 // CỐ GẮNG LẤY ID KHÁCH HÀNG VỪA LƯU
                 // Chỉ dùng TÊN làm identifier
                 com.starbucks.model.Customer customer = customerService.findCustomerByIdentifier(customerName); 
                 
                 if (customer != null) {
                    this.customerNameForTransaction = customer.getName();
                    this.customerIdForTransaction = customer.getCustomerId(); // Gắn ID cho giao dịch
                 }
            }
            
            // CHUYỂN SANG BƯỚC CHỌN PHƯƠNG THỨC THANH TOÁN
            showPaymentMethodDialog(); 

        } else {
            System.out.println("LOG: Đã hủy nhập thông tin khách hàng.");
        }
    }

    /**
     * Hiển thị hộp thoại chọn phương thức thanh toán (Cash/QR).
     */
    private void showPaymentMethodDialog() {
        int totalDue = currentOrderItems.stream().mapToInt(Product::getPrice).sum();
        String totalDueFormatted = String.format("%,d", totalDue);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chọn Phương thức Thanh toán");
        dialog.setHeaderText("Tổng số tiền cần thanh toán: " + totalDueFormatted + " VND (KH: " + customerNameForTransaction + ")");

        ButtonType cashButton = new ButtonType("Tiền Mặt (Cash)", ButtonData.OK_DONE);
        ButtonType qrButton = new ButtonType("Chuyển Khoản (QR)", ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(cashButton, qrButton, cancelButton);
        dialog.setContentText("Vui lòng chọn phương thức thanh toán:");

        dialog.showAndWait().ifPresent(result -> {
            if (result == cashButton) {
                // Xử lý tiền mặt
                showCashPaymentDialog(totalDue);
            } else if (result == qrButton) {
                // Xử lý QR
                handleFinalizeTransaction("QR", totalDue, totalDue); // Tiền khách trả = Tổng tiền
            }
        });
    }

    /**
     * Hiển thị hộp thoại nhập tiền khách trả và tính tiền thừa.
     */
    private void showCashPaymentDialog(int totalDue) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thanh toán Tiền Mặt");
        dialog.setHeaderText("Cần thanh toán: " + String.format("%,d", totalDue) + " VND");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField receivedField = new TextField();
        receivedField.setPromptText("Tiền khách đưa (VND)");
        
        Label changeLabel = new Label("Tiền thừa: 0 VND");
        changeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        receivedField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String cleanValue = newValue.replaceAll("[^\\d]", "");
                int receivedAmount = cleanValue.isEmpty() ? 0 : Integer.parseInt(cleanValue);
                int change = receivedAmount - totalDue;
                
                changeLabel.setText("Tiền thừa: " + String.format("%,d", change) + " VND");
                
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(change < 0);
                
            } catch (NumberFormatException e) {
                changeLabel.setText("Tiền thừa: Vui lòng nhập số.");
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            }
        });

        grid.add(new Label("Tiền khách đưa:"), 0, 0);
        grid.add(receivedField, 1, 0);
        grid.add(changeLabel, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    int receivedAmount = Integer.parseInt(receivedField.getText().replaceAll("[^\\d]", ""));
                    handleFinalizeTransaction("CASH", totalDue, receivedAmount);
                } catch (NumberFormatException e) {
                    new Alert(AlertType.ERROR, "Số tiền không hợp lệ. Giao dịch bị hủy.", ButtonType.OK).showAndWait();
                }
            }
        });
    }

    /** Hoàn tất giao dịch và reset */
    private void handleFinalizeTransaction(String paymentMethod, int totalDue, int receivedAmount) {
        
        // 1. TẠO ORDER DETAILS TỪ currentOrderItems
        List<OrderDetail> details = currentOrderItems.stream()
            .map(product -> new OrderDetail(
                product.getProductId(), // BẮT BUỘC: Lấy Product ID
                product.getName(),
                1, // Số lượng (Tạm thời cố định 1)
                product.getPrice()
            ))
            .collect(Collectors.toList());
        
        // 2. TẠO ORDER HEADER
        Order order = new Order(
            customerIdForTransaction, // Gắn ID Khách hàng
            customerNameForTransaction,
            totalDue,
            paymentMethod,
            details
        );
        
        // 3. LƯU VÀO DATABASE
        boolean success = orderService.saveOrder(order);

        // 4. XỬ LÝ KẾT QUẢ
        int change = receivedAmount - totalDue;
        
        if (success) {
            String message = (paymentMethod.equals("CASH")) ? 
                             "Đã nhận: " + String.format("%,d", receivedAmount) + " VND\nTiền thừa: " + String.format("%,d", change) + " VND" : 
                             "Đã xác nhận chuyển khoản.";
            
            Alert successAlert = new Alert(AlertType.INFORMATION);
            successAlert.setTitle("Giao dịch thành công!");
            successAlert.setHeaderText(null);
            successAlert.setContentText(message);
            successAlert.showAndWait();
            
        } else {
             Alert errorAlert = new Alert(AlertType.ERROR);
             errorAlert.setTitle("Lỗi lưu giao dịch!");
             errorAlert.setHeaderText("Thanh toán thành công nhưng không thể ghi nhận vào lịch sử DB.");
             errorAlert.setContentText("Vui lòng kiểm tra Console (Rollback).");
             errorAlert.showAndWait();
        }
        
        // 5. Reset giao diện và trạng thái khách hàng
        handleClearAll();
    }
    
    /** Xử lý nút EXIT (Thoát về Main Menu). */
    @FXML
    private void handleExitPOS() {
        System.out.println("LOG: Thoát khỏi màn hình POS, quay lại Main Menu.");
        Node source = productGrid;
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
