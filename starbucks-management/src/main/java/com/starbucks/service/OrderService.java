package com.starbucks.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.starbucks.db.DbConnector;
import com.starbucks.model.DailyReport;
import com.starbucks.model.Order;
import com.starbucks.model.OrderDetail;

public class OrderService {

	private final String ORDER_TABLE = DbConnector.getOrderTableName();
	private final String ORDER_DETAIL_TABLE = DbConnector.getOrderDetailTableName();
	private static final String REPORT_TABLE = "daily_report";
	private static final String GUEST_CUSTOMER_ID = "GUEST0000000000"; // ID Khách Vãng Lai

	private MenuService menuService;
	private CustomerService customerService;

	public OrderService(MenuService menuService, CustomerService customerService) {
		this.menuService = menuService;
		this.customerService = customerService;
	}

	/**
	 * Lưu Order và các chi tiết món hàng liên quan vào Database trong một giao dịch.
	 */
	public boolean saveOrder(Order order) {
		Connection conn = null;
		boolean success = false;

		try {
			conn = DbConnector.getConnection();
			if (conn == null) {
				System.err.println("Lỗi: Không thể kết nối DB để lưu Order.");
				return false;
			}
			
			conn.setAutoCommit(false); 
			
			// 1. TẠO ID ĐƠN HÀNG VÀ CHUẨN BỊ ORDER CHÍNH
			String orderId = DbConnector.generateUniqueId();
			order.setOrderId(orderId);
			
			saveOrderHeader(conn, order);
			
			// 2. LƯU CHI TIẾT CÁC MÓN HÀNG
			saveOrderDetails(conn, orderId, order.getItems());
			
			// THÀNH CÔNG: COMMIT GIAO DỊCH
			conn.commit();
			success = true;
			System.out.println("LOG: Lưu Order thành công, ID: " + orderId);
			
		} catch (SQLException e) {
			System.err.println("LỖI GIAO DỊCH: Đang thực hiện Rollback (Hoàn tác). Lỗi SQL: " + e.getMessage());
			e.printStackTrace();
			try {
				if (conn != null) {
					conn.rollback(); // HOÀN TÁC NẾU CÓ LỖI
				}
			} catch (SQLException ex) {
				System.err.println("Lỗi khi thực hiện Rollback: " + ex.getMessage());
			}
		} finally {
			DbConnector.closeConnection(conn);
		}
		return success;
	}

	/**
	 * Lưu thông tin chung của Order (Header) vào bảng order_transaction.
	 */
	private void saveOrderHeader(Connection conn, Order order) throws SQLException {
		// SỬA ĐỔI: Thêm CAST (::uuid) cho order_id và customer_id
		String sql = "INSERT INTO " + ORDER_TABLE + 
					 " (order_id, customer_id, customer_name, total_amount, payment_method, order_date) " +
					 " VALUES (?::uuid, ?::uuid, ?, ?, ?, ?)";
		
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			// Vị trí 1: order_id (String UUID)
			pstmt.setString(1, order.getOrderId());
			
			// Vị trí 2: customer_id (Kiểm tra và truyền String UUID)
			String customerId = order.getCustomerId();
			if (customerId != null && !customerId.isEmpty()) {
				pstmt.setString(2, customerId); // ID Khách thành viên
			} else {
				pstmt.setString(2, GUEST_CUSTOMER_ID); // Gán ID Khách Vãng Lai
			}
			
			// Các vị trí còn lại giữ nguyên
			pstmt.setString(3, order.getCustomerName());
			pstmt.setInt(4, order.getTotalAmount());
			pstmt.setString(5, order.getPaymentMethod());
			pstmt.setTimestamp(6, Timestamp.valueOf(order.getOrderDate()));
			
			pstmt.executeUpdate();
		}
	}

	/**
	 * Lưu chi tiết từng món hàng (Detail) vào bảng order_item.
	 */
    private void saveOrderDetails(Connection conn, String orderId, List<OrderDetail> details) throws SQLException {		// SỬA ĐỔI: Thêm CAST (::uuid) cho detail_id, order_id, product_id
		String sql = "INSERT INTO " + ORDER_DETAIL_TABLE + 
					 " (detail_id, order_id, product_id, product_name, quantity, price_at_sale) " +
					 " VALUES (?::uuid, ?::uuid, ?, ?, ?, ?)"; // Đã loại bỏ ::uuid cho tham số thứ 3
		
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (OrderDetail detail : details) {
				
				if (detail.getProductId() == null || detail.getProductId().isEmpty()) {
					throw new SQLException("Product ID is missing for item: " + detail.getProductName());
				}

				// Tạo ID duy nhất cho mỗi chi tiết order
				detail.setDetailId(DbConnector.generateUniqueId());
				detail.setOrderId(orderId);
				
				// Truyền String UUID cho detail_id và order_id (Vị trí 1, 2)
				pstmt.setString(1, detail.getDetailId());
				pstmt.setString(2, detail.getOrderId());
				
				// Truyền mã sản phẩm cho product_id (Vị trí 3)
				pstmt.setString(3, detail.getProductId()); 
				
				// ... (Các vị trí khác giữ nguyên) ...
				pstmt.setString(4, detail.getProductName());
				pstmt.setInt(5, detail.getQuantity());
				pstmt.setInt(6, detail.getPriceAtSale());
				
				pstmt.addBatch();
			}
			pstmt.executeBatch(); // Thực thi tất cả các lệnh INSERT
		}
	}

	/**
	 * TÍNH TOÁN: Lấy thống kê số lượng bán và tổng tiền TỪ LẦN EOD CUỐI CÙNG (is_eod = false).
	 * @return Map<String, int[]> Map<Product_ID, [So_luong, Tong_tien]>
	 */
	public Map<String, int[]> getSalesStatistics() {
		Map<String, int[]> stats = new HashMap<>();
		Connection conn = null;

		String sql = "SELECT oi.product_id, oi.product_name, oi.quantity, oi.price_at_sale " +
					 "FROM " + ORDER_DETAIL_TABLE + " oi " +
					 "JOIN " + ORDER_TABLE + " ot ON oi.order_id = ot.order_id " +
					 "WHERE ot.is_eod = false"; // Lấy các đơn hàng chưa chốt sổ

		try {
			conn = DbConnector.getConnection();
			if (conn == null) return stats;

			try (PreparedStatement pstmt = conn.prepareStatement(sql);
				 ResultSet rs = pstmt.executeQuery()) {

				while (rs.next()) {
					String productId = rs.getString("product_id");
					int quantity = rs.getInt("quantity");
					int priceAtSale = rs.getInt("price_at_sale");
					int subtotal = quantity * priceAtSale;

					stats.compute(productId, (k, v) -> {
						if (v == null) {
							// [Số lượng, Tổng tiền]
							return new int[]{quantity, subtotal};
						} else {
							v[0] += quantity;
							v[1] += subtotal;
							return v;
						}
					});
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi SQL khi tải thống kê bán hàng: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DbConnector.closeConnection(conn);
		}
		return stats;
	}

	/**
	 * QUY TRÌNH EOD: Lưu Báo cáo Tổng kết và Đóng sổ các giao dịch.
	 * @param totalRevenue Tổng doanh thu cần lưu trữ
	 * @return boolean Thành công/Thất bại
	 */
	public boolean finalizeDailyReport(int totalRevenue, String employeeId) {
		Connection conn = null;
		boolean success = false;

		try {
			conn = DbConnector.getConnection();
			if (conn == null) return false;

			conn.setAutoCommit(false);

			// 1. TẠO VÀ LƯU BÁO CÁO TỔNG KẾT
			String reportId = DbConnector.generateUniqueId();
            Timestamp reportTime = Timestamp.valueOf(java.time.LocalDateTime.now());
			
			// SỬA LỖI: Thêm CAST (::uuid) cho report_id
			String insertReportSql = "INSERT INTO " + REPORT_TABLE + 
                                 " (report_id, report_date, total_revenue, employee_id) VALUES (?::uuid, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertReportSql)) {
            pstmt.setString(1, reportId);
            pstmt.setTimestamp(2, reportTime);
            pstmt.setInt(3, totalRevenue);
            // Vị trí 4: employee_id
            pstmt.setString(4, employeeId); 
            pstmt.executeUpdate();
        }

			// 2. ĐÓNG SỔ: Cập nhật cờ is_eod = true cho tất cả các order chưa được chốt sổ
			String updateEODSql = "UPDATE " + ORDER_TABLE + " SET is_eod = true WHERE is_eod = false";
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(updateEODSql);
			}

			conn.commit();
			success = true;
			System.out.println("✅ LOG: Đã tổng kết và chốt sổ (EOD) thành công. ID Báo cáo: " + reportId);

		} catch (SQLException e) {
			System.err.println("❌ LỖI EOD: Đang thực hiện Rollback. Lỗi SQL: " + e.getMessage());
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} finally {
            DbConnector.closeConnection(conn);
    }
        return success;
	}

    /**
     * Lấy lịch sử các báo cáo EOD đã lưu trong bảng daily_report.
     */
    public List<DailyReport> getEODHistory() {
        List<DailyReport> history = new ArrayList<>();
        Connection conn = null;

        // Cập nhật SQL: Lấy employee_id
    String sql = "SELECT report_id, report_date, total_revenue, employee_id FROM " + REPORT_TABLE + 
                 " ORDER BY report_date DESC";

    try {
        conn = DbConnector.getConnection();
        if (conn == null) return history;

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String employeeId = rs.getString("employee_id");
                // Giả định: Bạn đã có EmployeeService để lấy Tên từ ID
                // Hiện tại, dùng ID làm tên nếu không muốn sửa EmployeeService
                String employeeName = employeeId != null ? employeeId : "N/A"; 
                
                history.add(new DailyReport(
                    rs.getString("report_id"),
                    rs.getTimestamp("report_date").toLocalDateTime(),
                    rs.getInt("total_revenue"),
                    employeeId, // ID nhân viên
                    employeeName // Tên nhân viên
                ));
            }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tải Lịch sử EOD: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }
        return history;
    }
	
	// THÊM: Hàm lấy tổng doanh thu chưa chốt sổ
	public int getCurrentTotalRevenue() {
		// Hàm này sẽ gọi getSalesStatistics và tính tổng cột [1]
		Map<String, int[]> stats = getSalesStatistics();
		return stats.values().stream().mapToInt(arr -> arr[1]).sum();
	}
}