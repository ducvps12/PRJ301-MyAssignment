package com.acme.leavemgmt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkTodo {
  private Long id;
  private String title;
  private Long assigneeId;           // người được giao
  private LocalDate dueDate;
  private String priority;           // LOW|MEDIUM|HIGH
  private String status;             // OPEN|DOING|DONE|CANCELLED
  private String tags;               // CSV tags
  private String note;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // getters/setters
  public Long getId(){ return id; }
  public void setId(Long id){ this.id = id; }
  public String getTitle(){ return title; }
  public void setTitle(String title){ this.title = title; }
  public Long getAssigneeId(){ return assigneeId; }
  public void setAssigneeId(Long assigneeId){ this.assigneeId = assigneeId; }
  public LocalDate getDueDate(){ return dueDate; }
  public void setDueDate(LocalDate dueDate){ this.dueDate = dueDate; }
  public String getPriority(){ return priority; }
  public void setPriority(String priority){ this.priority = priority; }
  public String getStatus(){ return status; }
  public void setStatus(String status){ this.status = status; }
  public String getTags(){ return tags; }
  public void setTags(String tags){ this.tags = tags; }
  public String getNote(){ return note; }
  public void setNote(String note){ this.note = note; }
  public LocalDateTime getCreatedAt(){ return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
  public LocalDateTime getUpdatedAt(){ return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }
}
