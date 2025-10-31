package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import static org.apache.tomcat.jakartaee.commons.compress.utils.TimeUtils.toDate;
import static org.apache.tomcat.jakartaee.commons.io.file.attribute.FileTimes.toDate;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Leave Request domain model. - Dùng LocalDate cho ngày; cung cấp alias
 * java.util.Date cho JSP fmt:formatDate. - Trạng thái nên lưu lowercase trong
 * DB: pending/approved/rejected/cancelled. - Có alias để tương thích JSP cũ:
 * getFrom(), getTo(), getFullName(), getType().
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Trạng thái gợi ý dùng
     */
    public static final String ST_PENDING = "pending";
    public static final String ST_APPROVED = "approved";
    public static final String ST_REJECTED = "rejected";
    public static final String ST_CANCELLED = "cancelled";
// Request.java
    private String leaveTypeName; // tên hiển thị
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public String getLeaveTypeName() {
        if (leaveTypeName != null && !leaveTypeName.isBlank()) {
            return leaveTypeName;
        }
        // fallback từ type/code nếu bạn đã có
        String t = getType(); // ví dụ field "type" hiện có
        if (t == null) {
            return null;
        }
        return switch (t) {
            case "AL", "ANNUAL" ->
                "Annual Leave (Phép năm)";
            case "SL", "SICK" ->
                "Sick Leave (Nghỉ ốm)";
            case "UL", "UNPAID" ->
                "Unpaid Leave (Không lương)";
            default ->
                t;
        };
    }

    public void setLeaveTypeName(String name) {
        this.leaveTypeName = name;
    }

    private int id;

    /**
     * Optional title cho UI
     */
    private String title;

    /**
     * Loại nghỉ (Annual, Sick, WFH, …) – thêm để khớp JSP cũ
     */
    private String type;

    private String reason;

    /**
     * Ngày dùng java.time
     */
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Trạng thái: pending/approved/rejected/cancelled (khuyến nghị lowercase
     * trong DB)
     */
    private String status;

    /**
     * Người tạo
     */
    private int createdBy;
    private String createdByName;   // JOIN từ Users

    /**
     * Người xử lý
     */
    private Integer processedBy;    // nullable
    private String processedByName; // JOIN từ Users

    /**
     * Ghi chú của quản lý khi duyệt/từ chối
     */
    private String managerNote;

    /**
     * Phòng ban & người duyệt đầu tiên (để filter/logic phê duyệt)
     */
    private String department;
    private int managerId;

    /**
     * Đính kèm (nếu có)
     */
    private String attachmentName;

    private List<RequestHistory> history;

    // ---------------- Constructors ----------------
    public Request() {
    }

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

    public long getDays() {
        LocalDate s = getStartDate();  // dùng getter thay vì truy cập field
        LocalDate e = getEndDate();
        if (s == null || e == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(s, e) + 1;
    }

    // Fields
    private String attachmentUrl;   // hoặc attachmentPath nếu bạn thích lưu path nội bộ
    private String attachmentPath;

// --- Attachment (tạm thời, có thể chưa map DB) ---
    public String getAttachmentUrl() {
        return null;
    }     // TODO: map thật sau

    public String getAttachmentPath() {
        return null;
    }     // TODO: map thật sau

    public String getAttachmentName() {
        return null;
    }     // TODO: map thật sau
// Nếu JSP có dùng ${r.hasAttachment}, thêm luôn:

    public boolean isHasAttachment() {
        return false; // TODO: khi map thật thì trả theo Url/Path/Name
    }

    /**
     * Copy constructor
     */
    public Request(Request other) {
        if (other == null) {
            return;
        }
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
    public Date getStartDateDate() {
        return startDate == null ? null : java.sql.Date.valueOf(startDate);
    }

    public Date getEndDateDate() {
        return endDate == null ? null : java.sql.Date.valueOf(endDate);
    }

    // Back-compat alias (nếu JSP cũ gọi tên này)
    public Date getStartDateUtil() {
        return getStartDateDate();
    }

    public Date getEndDateUtil() {
        return getEndDateDate();
    }

    /**
     * Alias để khớp JSP cũ dùng r.from / r.to
     */
    public Date getFrom() {
        return getStartDateDate();
    }

    public Date getTo() {
        return getEndDateDate();
    }

    /**
     * Alias để khớp JSP cũ dùng r.fullName
     */
    public String getFullName() {
        return createdByName;
    }

    /**
     * Alias để khớp JSP cũ dùng r.type
     */
    public String getType() {
        return type;
    }

    // ---------------- Business helpers ----------------
    /**
     * Tổng số ngày (bao gồm cả ngày bắt đầu & kết thúc); 0 nếu thiếu ngày
     */
    public long getTotalDays() {
        if (startDate == null || endDate == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public String getStatusUpper() {
        return status == null ? null : status.toUpperCase();
    }

    public String getStatusLower() {
        return status == null ? null : status.toLowerCase();
    }

    public boolean isPending() {
        return ST_PENDING.equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return ST_APPROVED.equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return ST_REJECTED.equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return ST_CANCELLED.equalsIgnoreCase(status);
    }

    /**
     * Có hiệu lực bao phủ ngày d cho yêu cầu đã APPROVED (bao gồm 2 đầu)
     */
    public boolean isActiveOn(LocalDate d) {
        if (!isApproved() || d == null || startDate == null || endDate == null) {
            return false;
        }
        return !d.isBefore(startDate) && !d.isAfter(endDate);
    }

    /**
     * Khoảng ngày [a,b] có giao với request này không (không xét status)
     */
    public boolean overlaps(LocalDate a, LocalDate b) {
        if (a == null || b == null || startDate == null || endDate == null) {
            return false;
        }
        if (b.isBefore(a)) { // hoán đổi nếu truyền ngược
            LocalDate tmp = a;
            a = b;
            b = tmp;
        }
        return !(endDate.isBefore(a) || startDate.isAfter(b));
    }

    public boolean hasAttachment() {
        return attachmentName != null && !attachmentName.isBlank();
    }

    // ---------------- Getters / Setters ----------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public String getProcessedByName() {
        return processedByName;
    }

    public void setProcessedByName(String processedByName) {
        this.processedByName = processedByName;
    }

    public String getManagerNote() {
        return managerNote;
    }

    public void setManagerNote(String managerNote) {
        this.managerNote = managerNote;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public void setAttachmentName(String v) {
        this.attachmentName = v;
    }

    public List<RequestHistory> getHistory() {
        return history;
    }

    public void setHistory(List<RequestHistory> history) {
        this.history = history;
    }

    // ---------------- Object contracts ----------------
    @Override
    public String toString() {
        return "Request{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", type='" + type + '\''
                + ", reason='" + reason + '\''
                + ", startDate=" + startDate
                + ", endDate=" + endDate
                + ", status='" + status + '\''
                + ", createdBy=" + createdBy
                + ", createdByName='" + createdByName + '\''
                + ", processedBy=" + processedBy
                + ", processedByName='" + processedByName + '\''
                + ", department='" + department + '\''
                + ", managerId=" + managerId
                + ", attachmentName='" + attachmentName + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Request)) {
            return false;
        }
        Request request = (Request) o;
        return id == request.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- Alias cho DAO cũ dùng setFrom()/setTo() ---
    public void setFrom(LocalDate fromDate) {
        this.startDate = fromDate;
    }

    public void setTo(LocalDate toDate) {
        this.endDate = toDate;
    }

    // === Fields ===
    private Long approverId;           // dùng Long để cho phép null
    private Long requesterId;          // dùng Long để cho phép null
    private String requesterDepartment;

    // === Getters (EL/JSP cần các getter này) ===
    public Long getApproverId() {
        return approverId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public String getRequesterDepartment() {
        return requesterDepartment;
    }

    // === Setters — FIX lỗi UnsupportedOperationException ===
    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    // Giữ nguyên chữ ký bạn đang có (long) và hỗ trợ cả Long nếu cần
    public void setRequesterId(long requesterId) {
        this.requesterId = requesterId; // auto-box sang Long
    }

    // overload phòng khi nơi khác truyền vào Long (có thể null)
    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public void setRequesterDepartment(String requesterDepartment) {
        this.requesterDepartment = (requesterDepartment == null)
                ? null
                : requesterDepartment.trim();
    }

    // CÁI BẠN ĐANG THIẾU: setApprovedAt(...)
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

// *** CÁI ĐANG THIẾU ***
    public LocalDateTime getCreatedAt() {
        LocalDateTime createdAt = null;
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // (tuỳ chọn) fluent style cho tiện khi map từ ResultSet
    public Request withApproverId(Long v) {
        this.approverId = v;
        return this;
    }

    public Request withRequesterId(Long v) {
        this.requesterId = v;
        return this;
    }

    public Request withRequesterDepartment(String v) {
        this.requesterDepartment = (v == null ? null : v.trim());
        return this;
    }

private Integer userId;

public void setUserId(Integer id) {
    this.userId = id;
}

  public Integer getUserId() {
    return userId;
}


}
