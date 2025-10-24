package com.starbucks.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.starbucks.db.DbConnector;
import com.starbucks.model.Customer;
import com.starbucks.model.Order;
import com.starbucks.model.OrderDetail;

public class CustomerService {

    private final String CUSTOMER_TABLE = DbConnector.getCustomerTableName();
    private final String ORDER_TABLE = DbConnector.getOrderTableName();
    private final String ORDER_DETAIL_TABLE = DbConnector.getOrderDetailTableName();

    public CustomerService() {
        // Constructor trống rỗng
    }

    /**
     * Tải toàn bộ danh sách khách hàng từ database
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        Connection conn = DbConnector.getConnection();
        if (conn == null) return customers;

        // BỎ phone_number khỏi SELECT
        String sql = "SELECT customer_id, name, reward_points, last_order_date FROM " + CUSTOMER_TABLE;

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("last_order_date");
                LocalDateTime lastOrderDate = (ts != null) ? ts.toLocalDateTime() : null;

                // Constructor MỚI: Bỏ phoneNumber
                Customer customer = new Customer(
                    rs.getString("customer_id"),
                    rs.getString("name"),
                    rs.getInt("reward_points"),
                    lastOrderDate
                );
                customers.add(customer);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tải danh sách khách hàng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }
        return customers;
    }
    
    /**
     * Tìm kiếm khách hàng bằng TÊN hoặc ID (ĐÃ BỎ SĐT).
     */
    public Customer findCustomerByIdentifier(String identifier) {
        Connection conn = null;
        Customer customer = null;
        
        // BỎ phone_number khỏi SQL
        String sql = "SELECT customer_id, name, reward_points, last_order_date FROM " + CUSTOMER_TABLE + 
                     " WHERE customer_id = ? OR name = ?"; 

        try {
            conn = DbConnector.getConnection();
            if (conn == null) return null;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, identifier); 
                pstmt.setString(2, identifier); 
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Timestamp ts = rs.getTimestamp("last_order_date");
                        LocalDateTime lastOrderDate = (ts != null) ? ts.toLocalDateTime() : null;

                        // Constructor KHÔNG có phoneNumber
                        customer = new Customer(
                            rs.getString("customer_id"),
                            rs.getString("name"),
                            rs.getInt("reward_points"),
                            lastOrderDate
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm khách hàng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }
        return customer;
    }
    
    /**
     * Lấy lịch sử giao dịch (Order History) của một khách hàng dựa trên ID.
     */
    public List<Order> getOrderHistoryByCustomerId(String customerId) {
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        
        String sql = "SELECT * FROM " + ORDER_TABLE + 
                     " WHERE customer_id = ? ORDER BY order_date DESC";

        try {
            conn = DbConnector.getConnection();
            if (conn == null) return orders;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, customerId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Order order = new Order();
                        String orderId = rs.getString("order_id");
                        
                        // Lấy thông tin Order Header
                        order.setOrderId(orderId);
                        order.setCustomerId(rs.getString("customer_id"));
                        order.setCustomerName(rs.getString("customer_name"));
                        order.setTotalAmount(rs.getInt("total_amount"));
                        order.setPaymentMethod(rs.getString("payment_method"));
                        
                        Timestamp ts = rs.getTimestamp("order_date");
                        order.setOrderDate(ts.toLocalDateTime());

                        // Lấy chi tiết món hàng (Order Details)
                        List<OrderDetail> details = getOrderDetailsByOrderId(conn, orderId);
                        order.setItems(details);
                        
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử giao dịch: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }
        return orders;
    }

    /**
     * Hàm helper để lấy chi tiết món hàng (Order Detail) của một Order cụ thể.
     */
    private List<OrderDetail> getOrderDetailsByOrderId(Connection conn, String orderId) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM " + ORDER_DETAIL_TABLE + " WHERE order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail(
                        rs.getString("detail_id"),
                        rs.getString("order_id"),
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getInt("price_at_sale")
                    );
                    details.add(detail);
                }
            }
        }
        return details;
    }
    
    /**
     * TRIỂN KHAI LOGIC LƯU DATABASE CHO saveOrUpdateCustomer (ĐÃ BỎ SĐT).
     * @param name Tên khách hàng.
     * @param phoneNumber SỐ ĐIỆN THOẠI (Tham số này KHÔNG ĐƯỢC DÙNG trong hàm).
     */
    public boolean saveOrUpdateCustomer(String name) { // GIỮ NGUYÊN PHONE NUMBER Ở ĐÂY ĐỂ TRÁNH PHÁ VỠ POSController
        Connection conn = null;
        boolean success = false;

        try {
            conn = DbConnector.getConnection();
            if (conn == null) return false;

            Customer existingCustomer = findCustomerByName(name);
            
            // Lệnh SQL (BỎ PHONE_NUMBER)
            String insertSql = "INSERT INTO " + CUSTOMER_TABLE + 
                             " (customer_id, name, reward_points, last_order_date) VALUES (?, ?, ?, ?)";
            // SỬA: Cập nhật TĂNG ĐIỂM +1
            String updateSql = "UPDATE " + CUSTOMER_TABLE + 
                             " SET last_order_date = ?, reward_points = reward_points + 1 WHERE customer_id = ?";

            if (existingCustomer == null) {
                // INSERT (Thêm mới)
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, UUID.randomUUID().toString()); 
                    pstmt.setString(2, name);
                    pstmt.setInt(3, 1); // Điểm thưởng ban đầu là 1
                    pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    success = pstmt.executeUpdate() > 0;
                    if(success) System.out.println("LOG: Đã thêm khách hàng mới: " + name);
                }
            } else {
                // UPDATE (Tăng điểm thưởng +1)
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.setString(2, existingCustomer.getCustomerId());
                    success = pstmt.executeUpdate() > 0;
                    if(success) System.out.println("LOG: Đã cập nhật khách hàng: " + name + " (+1 điểm).");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lưu/cập nhật khách hàng:");
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }
        
        return success;
    }
    
    /**
     * Hàm helper để tìm kiếm khách hàng bằng tên (chỉ dùng nội bộ cho saveOrUpdateCustomer).
     */
    private Customer findCustomerByName(String name) {
        Connection conn = DbConnector.getConnection();
        if (conn == null) return null;

        // BỎ phone_number khỏi SELECT
        String sql = "SELECT customer_id, name, reward_points, last_order_date FROM " + CUSTOMER_TABLE + " WHERE name = ?";
        Customer customer = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("last_order_date");
                    LocalDateTime lastOrderDate = (ts != null) ? ts.toLocalDateTime() : null;

                    // Constructor KHÔNG có phoneNumber
                    customer = new Customer(
                        rs.getString("customer_id"),
                        rs.getString("name"),
                        rs.getInt("reward_points"),
                        lastOrderDate
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm tên khách hàng: " + e.getMessage());
        } finally {
            DbConnector.closeConnection(conn);
        }
        return customer;
    }
    
    /**
     * Hàm phụ trợ này không còn được gọi (đã bị xóa trong các Controller)
     */
    public boolean saveOrUpdateCustomer(Customer customer) {
        System.out.println("LOG: CustomerService called to update points for: " + customer.getName());
        return true; 
    }
}
