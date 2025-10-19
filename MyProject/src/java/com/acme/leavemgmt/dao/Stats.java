package com.acme.leavemgmt.dao;

// trong RequestDAO.java
public class Stats {
    int pendingCount;
    int approvedThisMonth;
    int approvalNumerator;
    int approvalDenominator;

    public int getPendingCount(){ return pendingCount; }
    public int getApprovedThisMonth(){ return approvedThisMonth; }
    public int getApprovalNumerator(){ return approvalNumerator; }
    public int getApprovalDenominator(){ return approvalDenominator; }
    public int getApprovalRate(){
        return approvalDenominator==0 ? 0
             : (int) Math.round(approvalNumerator*100.0/approvalDenominator);
    }

    // package-private setter cho DAO dùng
    void setPendingCount(int v){ pendingCount=v; }
    void setApprovedThisMonth(int v){ approvedThisMonth=v; }
    void setApprovalNumerator(int v){ approvalNumerator=v; }
    void setApprovalDenominator(int v){ approvalDenominator=v; }
}
