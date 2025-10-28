package com.acme.leavemgmt.dao;

import java.io.Serializable;
import java.util.Objects;

/** Thống kê cho Division Dashboard. */
public class Stats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int headcount;             // tổng nhân sự active trong phòng
    int pendingCount;          // đơn chờ duyệt
    int approvedThisMonth;     // đơn đã duyệt trong tháng (theo approved_at)
    int approvalNumerator;     // số đơn đã duyệt (tử)
    int approvalDenominator;   // tổng đơn (mẫu)

    // ==== Getters cho JSP/EL ====
    public int getHeadcount() { return headcount; }
    public int getPendingCount() { return pendingCount; }
    public int getApprovedThisMonth() { return approvedThisMonth; }
    public int getApprovalNumerator() { return approvalNumerator; }
    public int getApprovalDenominator() { return approvalDenominator; }

    /** Tỉ lệ duyệt đã làm tròn về int % (0..100). */
    public int getApprovalRate() {
        return approvalDenominator == 0 ? 0
             : (int) Math.round(approvalNumerator * 100.0 / approvalDenominator);
    }

    /** Tỉ lệ duyệt dạng số thực (0..100), tiện cho biểu đồ/formatNumber. */
    public double getApprovalRateValue() {
        return approvalDenominator == 0 ? 0d
             : (approvalNumerator * 100.0 / approvalDenominator);
    }

    // ==== Alias giữ tương thích nếu JSP cũ dùng tên khác ====
    public int getTotalApproved() { return approvalNumerator; }
    public int getTotalRequests() { return approvalDenominator; }

    // ==== Setters (package-private) cho DAO ====
    void setHeadcount(int v) { headcount = v; }
    void setPendingCount(int v) { pendingCount = v; }
    void setApprovedThisMonth(int v) { approvedThisMonth = v; }
    void setApprovalNumerator(int v) { approvalNumerator = v; }
    void setApprovalDenominator(int v) { approvalDenominator = v; }

    // ==== Builder tiện dụng trong DAO ====
    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private final Stats s = new Stats();
        public Builder headcount(int v){ s.setHeadcount(v); return this; }
        public Builder pendingCount(int v){ s.setPendingCount(v); return this; }
        public Builder approvedThisMonth(int v){ s.setApprovedThisMonth(v); return this; }
        public Builder approvalNumerator(int v){ s.setApprovalNumerator(v); return this; }
        public Builder approvalDenominator(int v){ s.setApprovalDenominator(v); return this; }
        public Stats build(){ return s; }
    }

    // ==== Optional: toString/equals/hashCode cho logging/test ====
    @Override public String toString() {
        return "Stats{headcount=" + headcount + ", pendingCount=" + pendingCount +
               ", approvedThisMonth=" + approvedThisMonth + ", approvalNumerator=" +
               approvalNumerator + ", approvalDenominator=" + approvalDenominator + '}';
    }
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Stats)) return false;
        Stats s=(Stats)o;
        return headcount==s.headcount && pendingCount==s.pendingCount &&
               approvedThisMonth==s.approvedThisMonth &&
               approvalNumerator==s.approvalNumerator &&
               approvalDenominator==s.approvalDenominator;
    }
    @Override public int hashCode(){
        return Objects.hash(headcount,pendingCount,approvedThisMonth,approvalNumerator,approvalDenominator);
    }
}
