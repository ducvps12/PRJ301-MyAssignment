package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Leave Request domain model.
 * - Dùng LocalDate cho ngày; cung cấp alias java.util.Date cho JSP fmt:formatDate.
 * - Trạng thái nên lưu lowercase trong DB: pending/approved/rejected/cancelled.
 * - Có alias để tương thích JSP cũ: getFrom(), getTo(), getFullName(), getType().
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Trạng thái gợi ý dùng */
    public static final String ST_PENDING   = "pending";
    public static final String ST_APPROVED  = "approved";
    public static final String ST_REJECTED  = "rejected";
    public static final String ST_CANCELLED = "cancelled";

    private int id;

    /** Optional title cho UI */
    private String title;

    /** Loại nghỉ (Annual, Sick, WFH, …) – thêm để khớp JSP cũ */
    private String type;

    private String reason;

    /** Ngày dùng java.time */
    private LocalDate startDate;
    private LocalDate endDate;

    /** Trạng thái: pending/approved/rejected/cancelled (khuyến nghị lowercase trong DB) */
    private String status;

    /** Người tạo */
    private int createdBy;
    private String createdByName;   // JOIN từ Users

    /** Người xử lý */
    private Integer processedBy;    // nullable
    private String processedByName; // JOIN từ Users

    /** Ghi chú của quản lý khi duyệt/từ chối */
    private String managerNote;

    /** Phòng ban & người duyệt đầu tiên (để filter/logic phê duyệt) */
    private String department;
    private int managerId;

    /** Đính kèm (nếu có) */
    private String attachmentName;

    private List<RequestHistory> history;

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

    /** Copy constructor */
    public Request(Request other) {
        if (other == null) return;
        this.id = other.id;
        this.title = other.title;
        this.type = other.type;
        this.reason = other.reason;
        this.startDate = other.startDate;
        this.endDate = other.endDate;
        this.status = other.status;
        this.createdBy = other.createdBy;
        this.createdByName = other.createdByName;
        this.processedBy = other.processedBy;
        this.processedByName = other.processedByName;
        this.managerNote = other.managerNote;
        this.department = other.department;
        this.managerId = other.managerId;
        this.attachmentName = other.attachmentName;
        this.history = other.history; // lưu ý: shallow copy
    }

    // -------- Helpers cho JSP (fmt:formatDate cần java.util.Date) --------
    public Date getStartDateDate() { return startDate == null ? null : java.sql.Date.valueOf(startDate); }
    public Date getEndDateDate()   { return endDate   == null ? null : java.sql.Date.valueOf(endDate); }

    // Back-compat alias (nếu JSP cũ gọi tên này)
    public Date getStartDateUtil() { return getStartDateDate(); }
    public Date getEndDateUtil()   { return getEndDateDate(); }

    /** Alias để khớp JSP cũ dùng r.from / r.to */
    public Date getFrom() { return getStartDateDate(); }
    public Date getTo()   { return getEndDateDate(); }

    /** Alias để khớp JSP cũ dùng r.fullName */
    public String getFullName() { return createdByName; }

    /** Alias để khớp JSP cũ dùng r.type */
    public String getType() { return type; }

    // ---------------- Business helpers ----------------
    /** Tổng số ngày (bao gồm cả ngày bắt đầu & kết thúc); 0 nếu thiếu ngày */
    public long getTotalDays() {
        if (startDate == null || endDate == null) return 0L;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public String getStatusUpper() { return status == null ? null : status.toUpperCase(); }
    public String getStatusLower() { return status == null ? null : status.toLowerCase(); }

    public boolean isPending()   { return ST_PENDING.equalsIgnoreCase(status); }
    public boolean isApproved()  { return ST_APPROVED.equalsIgnoreCase(status); }
    public boolean isRejected()  { return ST_REJECTED.equalsIgnoreCase(status); }
    public boolean isCancelled() { return ST_CANCELLED.equalsIgnoreCase(status); }

    /** Có hiệu lực bao phủ ngày d cho yêu cầu đã APPROVED (bao gồm 2 đầu) */
    public boolean isActiveOn(LocalDate d) {
        if (!isApproved() || d == null || startDate == null || endDate == null) return false;
        return !d.isBefore(startDate) && !d.isAfter(endDate);
    }

    /** Khoảng ngày [a,b] có giao với request này không (không xét status) */
    public boolean overlaps(LocalDate a, LocalDate b) {
        if (a == null || b == null || startDate == null || endDate == null) return false;
        if (b.isBefore(a)) { // hoán đổi nếu truyền ngược
            LocalDate tmp = a; a = b; b = tmp;
        }
        return !(endDate.isBefore(a) || startDate.isAfter(b));
    }

    public boolean hasAttachment() { return attachmentName != null && !attachmentName.isBlank(); }

    // ---------------- Getters / Setters ----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public void setType(String type) { this.type = type; }

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
    public void setDepartment(String department) { this.department = department; }

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String v) { this.attachmentName = v; }

    public List<RequestHistory> getHistory() { return history; }
    public void setHistory(List<RequestHistory> history) { this.history = history; }

    // ---------------- Object contracts ----------------
    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", reason='" + reason + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", createdBy=" + createdBy +
                ", createdByName='" + createdByName + '\'' +
                ", processedBy=" + processedBy +
                ", processedByName='" + processedByName + '\'' +
                ", department='" + department + '\'' +
                ", managerId=" + managerId +
                ", attachmentName='" + attachmentName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        Request request = (Request) o;
        return id == request.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
