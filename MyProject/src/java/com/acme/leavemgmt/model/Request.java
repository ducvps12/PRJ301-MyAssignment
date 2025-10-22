package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;

    // Optional title cho UI
    private String title;

    private String reason;

    // Ngày dùng java.time
    private LocalDate startDate;
    private LocalDate endDate;

    // Trạng thái: pending/approved/rejected/cancelled (khuyến nghị lowercase trong DB)
    private String status;

    // Người tạo
    private int createdBy;
    private String createdByName;   // JOIN từ Users

    // Người xử lý
    private Integer processedBy;    // nullable
    private String processedByName; // JOIN từ Users

    // Ghi chú của quản lý khi duyệt/từ chối
    private String managerNote;

    // Phòng ban và người duyệt đầu tiên (để filter/logic phê duyệt)
    private String department;
    private int managerId;

    // Đính kèm (nếu có)
    private String attachmentName;
    private List<RequestHistory> history;

    public Request() { }

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

    // -------- Helpers cho JSP (fmt:formatDate cần java.util.Date) --------
    public Date getStartDateDate() { return startDate == null ? null : java.sql.Date.valueOf(startDate); }
    public Date getEndDateDate()   { return endDate   == null ? null : java.sql.Date.valueOf(endDate); }

    // (Giữ lại nếu bạn đang gọi tên này ở JSP khác)
    public Date getStartDateUtil() { return getStartDateDate(); }
    public Date getEndDateUtil()   { return getEndDateDate(); }

    public long getTotalDays() {
        if (startDate == null || endDate == null) return 0L;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    public String getStatusUpper() { return status == null ? null : status.toUpperCase(); }
    public String getStatusLower() { return status == null ? null : status.toLowerCase(); }

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

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }  // <-- IMPLEMENTED

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }        // <-- IMPLEMENTED

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String v) { this.attachmentName = v; }
    public boolean isHasAttachment() { return attachmentName != null && !attachmentName.isBlank(); }

    public List<RequestHistory> getHistory() { return history; }
    public void setHistory(List<RequestHistory> history) { this.history = history; }

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
                ", department='" + department + '\'' +
                ", managerId=" + managerId +
                '}';
    }
}
