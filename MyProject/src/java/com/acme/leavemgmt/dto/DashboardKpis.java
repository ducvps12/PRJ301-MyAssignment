package com.acme.leavemgmt.dto;

public class DashboardKpis {
    private int totalEmployees;
    private int onLeaveToday;
    private int interns;
    private int contractEndingSoon;

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int v) { this.totalEmployees = v; }

    public int getOnLeaveToday() { return onLeaveToday; }
    public void setOnLeaveToday(int v) { this.onLeaveToday = v; }

    public int getInterns() { return interns; }
    public void setInterns(int v) { this.interns = v; }

    public int getContractEndingSoon() { return contractEndingSoon; }
    public void setContractEndingSoon(int v) { this.contractEndingSoon = v; }
}
