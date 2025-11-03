package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/** Lịch sử xử lý 1 đơn nghỉ phép */
public class RequestHistory implements Serializable {
    private int id;
    private int requestId;
    private String action;       // CREATED / APPROVED / REJECTED / CANCELED
    private String note;
    private int actedBy;
    private String actedByName;
    private LocalDateTime actedAt;

    // ==== getters/setters cơ bản ====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getActedBy() { return actedBy; }
    public void setActedBy(int actedBy) { this.actedBy = actedBy; }

    public String getActedByName() { return actedByName; }
    public void setActedByName(String actedByName) { this.actedByName = actedByName; }

    public LocalDateTime getActedAt() { return actedAt; }
    public void setActedAt(LocalDateTime actedAt) { this.actedAt = actedAt; }

    // ==== Bridges cho JSP/JSTL ====

    /** Trả Date để dùng với <fmt:formatDate>. */
    public Date getActedAtDate() {
        if (actedAt == null) return null;
        return Date.from(actedAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Trả chuỗi "dd/MM/yyyy HH:mm" (dùng trực tiếp bằng ${h.actedAtStr}). */
    public String getActedAtStr() {
        if (actedAt == null) return null;
        return actedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override public String toString() {
        return "RequestHistory{" +
                "id=" + id +
                ", requestId=" + requestId +
                ", action='" + action + '\'' +
                ", note='" + note + '\'' +
                ", actedBy=" + actedBy +
                ", actedByName='" + actedByName + '\'' +
                ", actedAt=" + actedAt +
                '}';
    }
}
