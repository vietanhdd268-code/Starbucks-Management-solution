package com.starbucks.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.starbucks.db.DbConnector;
import com.starbucks.model.Employee;

public class EmployeeService {
    
    // Sử dụng hàm getEmployeeTableName()
    private final String TABLE_NAME = DbConnector.getEmployeeTableName(); 

    public Employee login(String id, String password) {
        Connection conn = DbConnector.getConnection();
        if (conn == null) return null;

        String sql = "SELECT id, name, password, role FROM " + TABLE_NAME + " WHERE id = ? AND password = ?";
        Employee employee = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Đọc dữ liệu từ Database
                String dbId = rs.getString("id");
                String dbName = rs.getString("name");
                String dbPassword = rs.getString("password"); 
                String dbRole = rs.getString("role");
                
                employee = new Employee(dbId, dbName, dbPassword, dbRole);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đăng nhập:");
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }

        return employee;
    }

    public boolean isAuthorizedManager(String id) {
        Connection conn = DbConnector.getConnection();
        if (conn == null) return false;

        // Chỉ cần kiểm tra xem có ID này không và Role của họ là j
        String sql = "SELECT role FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                // Kiểm tra nếu là SM hoặc IC
                return "SM".equalsIgnoreCase(role) || "IC".equalsIgnoreCase(role);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi kiểm tra vai trò:");
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }
        
        return false;
    }

    public boolean changePassword(String id, String oldPassword, String newPassword) {
        Connection conn = DbConnector.getConnection();
        if (conn == null) return false;

        // Kiểm tra mật khẩu cũ và cập nhật mật khẩu mới
        String sql = "UPDATE " + TABLE_NAME + " SET password = ? WHERE id = ? AND password = ?";
        int rowsAffected = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, id);
            pstmt.setString(3, oldPassword);
            
            rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("LOG: Mật khẩu của " + id + " đã được thay đổi.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đổi mật khẩu:");
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }

        return rowsAffected > 0;
    }

    public boolean resetPassword(String id) {
        Connection conn = DbConnector.getConnection();
        if (conn == null) return false;

        String defaultPassword = "1";

        String sql = "UPDATE " + TABLE_NAME + " SET password = ? WHERE id = ?"; 
        int rowsAffected = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, defaultPassword);
            pstmt.setString(2, id);
            
            rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("LOG: Mật khẩu của " + id + " đã được đặt lại thành: " + defaultPassword);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi reset mật khẩu:");
            e.printStackTrace();
        } finally {
            DbConnector.closeConnection(conn);
        }

        return rowsAffected > 0;
    }
}
