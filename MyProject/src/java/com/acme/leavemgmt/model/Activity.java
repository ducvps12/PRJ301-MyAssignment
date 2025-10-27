package com.acme.leavemgmt.model;

import java.util.Date;

public class Activity {
  private int id;
  private Integer userId;
  private String action;
  private String entityType;
  private Integer entityId;
  private String note;
  private String ipAddr;
  private String userAgent;
  private Date createdAt;

  // getters/setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  public Integer getUserId() { return userId; }
  public void setUserId(Integer userId) { this.userId = userId; }
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  public String getEntityType() { return entityType; }
  public void setEntityType(String entityType) { this.entityType = entityType; }
  public Integer getEntityId() { return entityId; }
  public void setEntityId(Integer entityId) { this.entityId = entityId; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public String getIpAddr() { return ipAddr; }
  public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
  public String getUserAgent() { return userAgent; }
  public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
  public Date getCreatedAt() { return createdAt; }
  public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
