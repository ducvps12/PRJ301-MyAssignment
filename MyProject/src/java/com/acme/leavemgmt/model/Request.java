package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Leave Request domain model.
 * Compatible with schema V1: Requests(id, user_id, type, status, reason, start_date, end_date, ...)
 */
public class Request implements Serializable {

    private int id;

    // Optional "title" for UI (DB V1 không có cột title -> có thể null)
    private String title;

    private String reason;

    // Dữ liệu ngày dạng java.time (an toàn timezone)
    private LocalDate startDate;
    private LocalDate endDate;

    // Trạng thái (khuyến nghị: DB lưu lowercase: pending/approved/rejected/cancelled)
    private String status;

    // Người tạo
    private int createdBy;
    private String createdByName;     // JOIN từ Users.full_name

    // Người xử lý (có thể không dùng ở schema V1)
    private Integer processedBy;      // nullable
    private String processedByName;   // JOIN từ Users.full_name

    // Ghi chú của quản lý khi duyệt/từ chối
    private String managerNote;

    // ---------------- Constructors ----------------
    public Request() {}

    public Request(int id, String reason, LocalDate startDate, LocalDate endDate,
                   String status, int createdBy, String createdByName) {
        this.id = id;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
    }

    // ---------------- Getters / Setters ----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public Integer getProcessedBy() { return processedBy; }
    public void setProcessedBy(Integer processedBy) { this.processedBy = processedBy; }

    public String getProcessedByName() { return processedByName; }
    public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }

    public String getManagerNote() { return managerNote; }
    public void setManagerNote(String managerNote) { this.managerNote = managerNote; }

    // ---------------- Helpers for JSP / UI ----------------

    /** fmt:formatDate chỉ nhận java.util.Date → helper chuyển từ LocalDate */
    public java.util.Date getStartDateDate() {
        return startDate == null ? null : java.sql.Date.valueOf(startDate);
    }

    public java.util.Date getEndDateDate() {
        return endDate == null ? null : java.sql.Date.valueOf(endDate);
    }

    /** Số ngày nghỉ (đã bao gồm cả ngày đầu/cuối) */
    public long getTotalDays() {
        if (startDate == null || endDate == null) return 0L;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /** Uppercase cho CSS pill trên UI */
    public String getStatusUpper() {
        return status == null ? null : status.toUpperCase();
    }

    /** Lowercase để lưu/so sánh với DB (CHECK constraint dùng chữ thường) */
    public String getStatusLower() {
        return status == null ? null : status.toLowerCase();
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", reason='" + reason + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", createdBy=" + createdBy +
                ", createdByName='" + createdByName + '\'' +
                '}';
    }
}
