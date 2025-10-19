package com.acme.leavemgmt.model;

import java.time.LocalDateTime;

public class RequestHistory {
    private int id;
    private int requestId;
    private String action;         // CREATED/APPROVED/...
    private String note;
    private int actedBy;
    private String actedByName;
    private LocalDateTime actedAt;

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
}
