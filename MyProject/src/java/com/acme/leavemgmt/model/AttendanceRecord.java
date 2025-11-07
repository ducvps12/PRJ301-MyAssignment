package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/** Bản ghi chấm công của 1 nhân sự trong 1 ngày. */
public class AttendanceRecord implements Serializable {
  private Long id;
  private Long userId;
  private LocalDate workDate;             // yyyy-MM-dd
  private LocalDateTime checkin;          // giờ vào
  private LocalDateTime checkout;         // giờ ra
  private String shiftCode;               // ca làm (A/B/C, WFH…)
  private String status;                  // PRESENT/ABSENT/LEAVE/REMOTE…
  private String note;                    // ghi chú ngắn

  private int lateMinutes;                // đi muộn (phút)
  private int earlyMinutes;               // về sớm (phút)
  private int otMinutes;                  // OT (phút)

  // (tuỳ nghi) dấu vết tạo/sửa
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /* ===== Constructors ===== */
  public AttendanceRecord() { }

  public AttendanceRecord(Long userId, LocalDate workDate) {
    this.userId = userId;
    this.workDate = workDate;
  }

  /* ===== Getters/Setters (field names như bạn) ===== */
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public LocalDate getWorkDate() { return workDate; }
  public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

  public LocalDateTime getCheckin() { return checkin; }
  public void setCheckin(LocalDateTime checkin) { this.checkin = checkin; }

  public LocalDateTime getCheckout() { return checkout; }
  public void setCheckout(LocalDateTime checkout) { this.checkout = checkout; }

  public String getShiftCode() { return shiftCode; }
  public void setShiftCode(String shiftCode) { this.shiftCode = shiftCode; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }

  public int getLateMinutes() { return lateMinutes; }
  public void setLateMinutes(int lateMinutes) { this.lateMinutes = lateMinutes; }

  public int getEarlyMinutes() { return earlyMinutes; }
  public void setEarlyMinutes(int earlyMinutes) { this.earlyMinutes = earlyMinutes; }

  public int getOtMinutes() { return otMinutes; }
  public void setOtMinutes(int otMinutes) { this.otMinutes = otMinutes; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  /* ===== Alias để tương thích DAO cũ (checkIn/checkOut/notes) ===== */
  public LocalDateTime getCheckIn() { return checkin; }
  public void setCheckIn(LocalDateTime dt) { this.checkin = dt; }

  public LocalDateTime getCheckOut() { return checkout; }
  public void setCheckOut(LocalDateTime dt) { this.checkout = dt; }

  public String getNotes() { return note; }
  public void setNotes(String n) { this.note = n; }

  /* ===== equals/hashCode/toString ===== */
  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AttendanceRecord)) return false;
    AttendanceRecord that = (AttendanceRecord) o;
    // coi (userId, workDate) là khóa tự nhiên
    return Objects.equals(userId, that.userId)
        && Objects.equals(workDate, that.workDate);
  }
  @Override public int hashCode() { return Objects.hash(userId, workDate); }

  @Override public String toString() {
    return "AttendanceRecord{" +
        "id=" + id +
        ", userId=" + userId +
        ", workDate=" + workDate +
        ", checkin=" + checkin +
        ", checkout=" + checkout +
        ", status='" + status + '\'' +
        ", late=" + lateMinutes +
        ", early=" + earlyMinutes +
        ", ot=" + otMinutes +
        '}';
  }

  /* ===== Builder tiện dùng ===== */
  public static Builder builder() { return new Builder(); }
  public static class Builder {
    private final AttendanceRecord r = new AttendanceRecord();
    public Builder id(Long v){ r.setId(v); return this; }
    public Builder userId(Long v){ r.setUserId(v); return this; }
    public Builder workDate(LocalDate v){ r.setWorkDate(v); return this; }
    public Builder checkin(LocalDateTime v){ r.setCheckin(v); return this; }
    public Builder checkout(LocalDateTime v){ r.setCheckout(v); return this; }
    public Builder shift(String v){ r.setShiftCode(v); return this; }
    public Builder status(String v){ r.setStatus(v); return this; }
    public Builder note(String v){ r.setNote(v); return this; }
    public Builder late(int v){ r.setLateMinutes(v); return this; }
    public Builder early(int v){ r.setEarlyMinutes(v); return this; }
    public Builder ot(int v){ r.setOtMinutes(v); return this; }
    public Builder created(LocalDateTime v){ r.setCreatedAt(v); return this; }
    public Builder updated(LocalDateTime v){ r.setUpdatedAt(v); return this; }
    public AttendanceRecord build(){ return r; }
  }
}
