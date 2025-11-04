package com.acme.leavemgmt.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class TodayLeave implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String divisionName;
    private Date startDate; // java.util.Date để dùng trực tiếp với fmt:formatDate
    private Date endDate;

    public TodayLeave() {}

    public TodayLeave(String fullName, String divisionName, Date startDate, Date endDate) {
        this.fullName = fullName;
        this.divisionName = divisionName;
        // defensive copy (Date là mutable)
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
        this.endDate   = endDate   != null ? new Date(endDate.getTime())   : null;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }

    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
    }

    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
    }

    @Override
    public String toString() {
        return "TodayLeave{" +
               "fullName='" + fullName + '\'' +
               ", divisionName='" + divisionName + '\'' +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodayLeave)) return false;
        TodayLeave that = (TodayLeave) o;
        return Objects.equals(fullName, that.fullName) &&
               Objects.equals(divisionName, that.divisionName) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, divisionName, startDate, endDate);
    }
}
