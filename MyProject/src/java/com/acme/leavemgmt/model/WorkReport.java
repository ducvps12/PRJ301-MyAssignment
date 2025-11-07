package com.acme.leavemgmt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkReport {
  private Long id;
  private Long userId;
  private LocalDate workDate;
  /** DAILY | WEEKLY | MONTHLY | LEAVE_NOTE ... */
  private String type;
  private Integer hours;           // tổng giờ làm (optional)
  private String content;          // nội dung báo cáo
  private String tags;             // CSV tags
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // getters/setters
  public Long getId(){ return id; }
  public void setId(Long id){ this.id = id; }
  public Long getUserId(){ return userId; }
  public void setUserId(Long userId){ this.userId = userId; }
  public LocalDate getWorkDate(){ return workDate; }
  public void setWorkDate(LocalDate workDate){ this.workDate = workDate; }
  public String getType(){ return type; }
  public void setType(String type){ this.type = type; }
  public Integer getHours(){ return hours; }
  public void setHours(Integer hours){ this.hours = hours; }
  public String getContent(){ return content; }
  public void setContent(String content){ this.content = content; }
  public String getTags(){ return tags; }
  public void setTags(String tags){ this.tags = tags; }
  public LocalDateTime getCreatedAt(){ return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
  public LocalDateTime getUpdatedAt(){ return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }

    public void setHours(BigDecimal h) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
