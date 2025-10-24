package com.starbucks.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.util.UUID; 

public class DbConnector {
    
    private static final String URL = "jdbc:postgresql://db.zmnnipsdruhejhulupfw.supabase.co:5432/postgres"; 
    
    private static final String USER = "postgres"; 
    
    private static final String PASSWORD = "1234"; 
    
    private static final String TABLE_NAME_EMPLOYEE = "employee"; 
    private static final String TABLE_NAME_PRODUCT = "product"; 
    private static final String TABLE_NAME_CUSTOMER = "customer";
    private static final String TABLE_NAME_ORDER = "order_transaction"; 
    private static final String TABLE_NAME_ORDER_DETAIL = "order_item"; 

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("LOG: Kết nối thành công tới Supabase PostgreSQL!");
            return conn;
        } catch (SQLException e) { 
            System.err.println("LỖI KẾT NỐI DATABASE: Vui lòng kiểm tra mật khẩu, URL, và kết nối Internet.");
            e.printStackTrace();
            return null;
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // FIX LỖI BIÊN DỊCH
    public static String getEmployeeTableName() {
        return TABLE_NAME_EMPLOYEE;
    }

    public static String getProductTableName() {
        return TABLE_NAME_PRODUCT;
    }

    public static String getCustomerTableName() {
        return TABLE_NAME_CUSTOMER;
    }

    public static String getOrderTableName() {
        return TABLE_NAME_ORDER;
    }

    public static String getOrderDetailTableName() {
        return TABLE_NAME_ORDER_DETAIL;
    }
    
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
