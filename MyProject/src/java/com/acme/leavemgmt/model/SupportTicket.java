package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.util.Date;

public class SupportTicket implements Serializable {
  private int id;
  private Integer userId;
  private String userName;
  private String email;
  private String title;
  private String body;
  private String techJson;
  private String status; // OPEN|INPROGRESS|RESOLVED|CLOSED
  private Date createdAt;
  private Date handledAt;
  private Integer handledBy;
  private String note;

  // getters/setters (Date defensive copy)
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  public Integer getUserId() { return userId; }
  public void setUserId(Integer userId) { this.userId = userId; }
  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getBody() { return body; }
  public void setBody(String body) { this.body = body; }
  public String getTechJson() { return techJson; }
  public void setTechJson(String techJson) { this.techJson = techJson; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Date getCreatedAt() { return createdAt == null ? null : new Date(createdAt.getTime()); }
  public void setCreatedAt(Date createdAt) { this.createdAt = createdAt == null ? null : new Date(createdAt.getTime()); }
  public Date getHandledAt() { return handledAt == null ? null : new Date(handledAt.getTime()); }
  public void setHandledAt(Date handledAt) { this.handledAt = handledAt == null ? null : new Date(handledAt.getTime()); }
  public Integer getHandledBy() { return handledBy; }
  public void setHandledBy(Integer handledBy) { this.handledBy = handledBy; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
}
