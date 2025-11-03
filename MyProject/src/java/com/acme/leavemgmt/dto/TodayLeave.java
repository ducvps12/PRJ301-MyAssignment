package com.acme.leavemgmt.dto;

import java.util.Date;

public class TodayLeave {
    private String fullName;
    private String divisionName;
    private Date startDate;
    private Date endDate;

    public TodayLeave() {}

    public TodayLeave(String fullName, String divisionName, Date startDate, Date endDate) {
        this.fullName = fullName;
        this.divisionName = divisionName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
