package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Leave Request domain model
 * - Dùng LocalDate/LocalDateTime cho ngày & thời gian.
 * - Alias tương thích JSP cũ: getFrom(), getTo(), getFullName(), getType().
 * - Có helper tính số ngày, kiểm tra giao khoảng, trạng thái...
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    // ===== Trạng thái chuẩn =====
    public static final String ST_PENDING   = "PENDING";
    public static final String ST_APPROVED  = "APPROVED";
    public static final String ST_REJECTED  = "REJECTED";
    public static final String ST_CANCELLED = "CANCELLED";

    // ======= CORE (match dbo.Requests) =======
    private Integer id;                 // id (PK)
    private Integer userId;             // user_id (người tạo/requester)
    private String  type;               // ANNUAL/SICK/UNPAID/WFH...
    private String  reason;             // reason
    private String  status;             // PENDING/APPROVED/REJECTED/CANCELLED
    private LocalDate startDate;        // start_date
    private LocalDate endDate;          // end_date
    private LocalDateTime createdAt;    // created_at
    private LocalDateTime approvedAt;   // approved_at (nullable)
    private Integer processedBy;        // processed_by (nullable)
    private Integer approvedBy;         // approved_by (nullable)
    private Integer leaveTypeId;        // leave_type_id (nullable)
    private String  managerNote;        // manager_note (nullable)
    private String  approveNote;        // approve_note (nullable)
    private String  title;              // title (nullable)
    private String  attachmentName;     // nếu có file đính kèm (nullable)

    // ======= JOIN/VIEW tiện cho UI =======
    private String  fullName;           // Users.full_name của requester
    private String  createdByName;      // alias nếu DAO map tên tạo
    private String  processedByName;
    private String  approvedByName;
    private String  department;         // tên phòng ban
    private Integer departmentId;       // id phòng ban
    private Integer managerId;          // id quản lý trực tiếp
    private String  requesterDepartment;// alias: requester_department
    private String  leaveTypeName;      // tên loại phép nếu JOIN

    // ======= Lịch sử =======
    private List<RequestHistory> history;

    // ========= Constructors =========
    public Request() {}

    public Request(int id, String reason, LocalDate start, LocalDate end,
                   String status, int userId, String createdByName) {
        this.id = id;
        this.reason = reason;
        this.startDate = start;
        this.endDate = end;
        this.status = status;
        this.userId = userId;
        this.createdByName = createdByName;
    }

    // ========= Helpers =========
    /** Số ngày nghỉ, tính cả hai đầu. */
    public long getDays() {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /** Tổng số ngày (alias). */
    public long getTotalDays() { return getDays(); }

    /** Có giao với khoảng [a,b] không (không xét status). */
    public boolean overlaps(LocalDate a, LocalDate b) {
        if (a == null || b == null || startDate == null || endDate == null) return false;
        if (b.isBefore(a)) { LocalDate t = a; a = b; b = t; }
        return !(endDate.isBefore(a) || startDate.isAfter(b));
    }

    /** Đơn đã APPROVED có hiệu lực bao phủ ngày d không. */
    public boolean isActiveOn(LocalDate d) {
        if (!isApproved() || d == null) return false;
        return !d.isBefore(startDate) && !d.isAfter(endDate);
    }

    public boolean isPending()   { return ST_PENDING.equalsIgnoreCase(status); }
    public boolean isApproved()  { return ST_APPROVED.equalsIgnoreCase(status); }
    public boolean isRejected()  { return ST_REJECTED.equalsIgnoreCase(status); }
    public boolean isCancelled() { return ST_CANCELLED.equalsIgnoreCase(status); }

    public boolean hasAttachment() { return attachmentName != null && !attachmentName.isBlank(); }

    // ===== Alias cho JSP cũ =====
    /** JSP thường dùng ${r.from} / ${r.to} với fmt:formatDate → trả Date (java.sql.Date). */
    public Date getFrom() { return startDate == null ? null : java.sql.Date.valueOf(startDate); }
    public Date getTo()   { return endDate   == null ? null : java.sql.Date.valueOf(endDate);   }
    public String getFullName() { return fullName != null ? fullName : createdByName; }
    public String getType()     { return type; }

    /** Nếu dùng trực tiếp fmt:formatDate với start/end. */
    public Date getStartDateDate() { return getFrom(); }
    public Date getEndDateDate()   { return getTo();   }

    /** ===== Compat cho JSP đang gọi ${req.startDateUtil} / ${req.endDateUtil} ===== */
    public Date getStartDateUtil() {
        if (startDate == null) return null;
        return Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    public Date getEndDateUtil() {
        if (endDate == null) return null;
        return Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Chuỗi dải ngày gọn để hiển thị trực tiếp. */
    public String getDateRangeText() {
        if (startDate == null && endDate == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (startDate != null && endDate != null) {
            return startDate.equals(endDate)
                    ? startDate.format(fmt)
                    : startDate.format(fmt) + " – " + endDate.format(fmt);
        }
        return (startDate != null ? startDate.format(fmt) : "")
                + (endDate != null ? " – " + endDate.format(fmt) : "");
    }

    /** ISO string tiện log/API. */
    public String getStartDateISO() { return startDate == null ? null : startDate.toString(); }
    public String getEndDateISO()   { return endDate   == null ? null : endDate.toString();   }

    // ===== LeaveType name fallback nếu không JOIN tên =====
    public String getLeaveTypeName() {
        if (leaveTypeName != null && !leaveTypeName.isBlank()) return leaveTypeName;
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "AL", "ANNUAL" -> "Annual Leave (Phép năm)";
            case "SL", "SICK"   -> "Sick Leave (Nghỉ ốm)";
            case "UL", "UNPAID" -> "Unpaid Leave (Không lương)";
            case "WFH"          -> "Work From Home";
            default              -> type;
        };
    }

    // ========= Getters / Setters =========
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    // Alias để DAO cũ map requester_id
    public Integer getRequesterId() { return userId; }
    public void setRequesterId(Integer v) { this.userId = v; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void setType(String type) { this.type = type; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // Alias cho DAO cũ dùng setFrom()/setTo() bằng LocalDate
    public void setFrom(LocalDate d) { this.startDate = d; }
    public void setTo(LocalDate d)   { this.endDate   = d; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public Integer getProcessedBy() { return processedBy; }
    public void setProcessedBy(Integer processedBy) { this.processedBy = processedBy; }

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }

    // Alias để DAO có thể gọi setApproverId(...)
    public Integer getApproverId() { return approvedBy; }
    public void setApproverId(Integer v) { this.approvedBy = v; }

    public Integer getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Integer leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public String getManagerNote() { return managerNote; }
    public void setManagerNote(String managerNote) { this.managerNote = managerNote; }

    public String getApproveNote() { return approveNote; }
    public void setApproveNote(String approveNote) { this.approveNote = approveNote; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public String getFullNameField() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getProcessedByName() { return processedByName; }
    public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public String getRequesterDepartment() { return requesterDepartment; }
    public void setRequesterDepartment(String requesterDepartment) {
        this.requesterDepartment = requesterDepartment == null ? null : requesterDepartment.trim();
    }

    public String getLeaveTypeNameRaw() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }

    public List<RequestHistory> getHistory() { return history; }
    public void setHistory(List<RequestHistory> history) { this.history = history; }


// ===== Attachment helpers for JSP =====

/** URL xem/tải tệp đính kèm. JSP sẽ tự nối contextPath ở trước. */
public String getAttachmentUrl() {
    if (!hasAttachment() || id == null) return null;
    // Nếu bạn có AttachmentServlet map "/request/attachment"
    return "/request/attachment?id=" + id;
    // Trường hợp bạn phục vụ file tĩnh theo tên, có thể dùng:
    // return "/uploads/requests/" + java.net.URLEncoder.encode(attachmentName, java.nio.charset.StandardCharsets.UTF_8);
}

/** Alias cho nút Download (nếu JSP gọi). */
public String getAttachmentDownloadUrl() {
    return getAttachmentUrl();
}

/** Nhãn hiển thị cho link file. */
public String getAttachmentLabel() {
    return (attachmentName == null || attachmentName.isBlank())
            ? "Tệp đính kèm"
            : attachmentName.trim();
}


    // ========= Object contracts =========
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        Request r = (Request) o;
        return Objects.equals(id, r.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", processedBy=" + processedBy +
                ", approvedBy=" + approvedBy +
                '}';
    }

    // map createdBy ↔ userId cho tương thích JSP/DAO cũ
    public void setCreatedBy(int createdBy) { this.userId = createdBy; }
    public Integer getCreatedBy() { return this.userId; }
}
