// src/main/java/com/acme/leavemgmt/dto/DivStats.java
package com.acme.leavemgmt.dto;

public class DivStats {
    private int headcount;
    private int pendingCount;
    private int approvedThisMonth;
    private int approvalNumerator;
    private int approvalDenominator;

    public int getHeadcount() { return headcount; }
    public void setHeadcount(int headcount) { this.headcount = headcount; }

    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

    public int getApprovedThisMonth() { return approvedThisMonth; }
    public void setApprovedThisMonth(int approvedThisMonth) { this.approvedThisMonth = approvedThisMonth; }

    public int getApprovalNumerator() { return approvalNumerator; }
    public void setApprovalNumerator(int approvalNumerator) { this.approvalNumerator = approvalNumerator; }

    public int getApprovalDenominator() { return approvalDenominator; }
    public void setApprovalDenominator(int approvalDenominator) { this.approvalDenominator = approvalDenominator; }
}
