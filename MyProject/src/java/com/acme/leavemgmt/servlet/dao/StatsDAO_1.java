package com.acme.leavemgmt.servlet.dao;

// com.acme.leavemgmt.dao.Stats
class Stats {
    public int pendingCount;
    public int approvedThisMonth;
    public int approvalNumerator;   // số Approved
    public int approvalDenominator; // số đã xử lý (Approved + Rejected)
}
