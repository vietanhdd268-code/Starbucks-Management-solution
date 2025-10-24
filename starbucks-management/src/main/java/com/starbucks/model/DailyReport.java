package com.starbucks.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DailyReport {
    private String reportId;
    private LocalDateTime reportDate;
    private int totalRevenue;
    // THÊM: employeeName để hiển thị (Giả định bạn có thể lấy tên người chốt)
    private String employeeId;
    private String employeeName; 

    public DailyReport(String reportId, LocalDateTime reportDate, int totalRevenue, String employeeId, String employeeName) {
        this.reportId = reportId;
        this.reportDate = reportDate;
        this.totalRevenue = totalRevenue;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    // Getters cho TableView
    public String getReportId() { return reportId; }
    public int getTotalRevenue() { return totalRevenue; }
    // Getter mới
    public String getEmployeeId() { return employeeId; } 
    public String getEmployeeName() { return employeeName; }
    
    // Getter cần thiết cho cột Ngày Chốt Sổ
    public String getReportDateString() {
        return reportDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}