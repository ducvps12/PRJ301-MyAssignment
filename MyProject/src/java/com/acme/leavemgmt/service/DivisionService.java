// src/main/java/com/acme/leavemgmt/service/DivisionService.java
package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.dto.DivStats;
import com.acme.leavemgmt.model.Request;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DivisionService {
    private final RequestDAO dao = new RequestDAO();

    public DivStats computeStats(String dept, LocalDate from, LocalDate to) throws SQLException {
        DivStats s = new DivStats();
        s.setHeadcount(dao.countHeadcountActiveByDept(dept));
        s.setPendingCount(dao.countPendingByDept(dept, from, to));
        s.setApprovedThisMonth(dao.countApprovedThisMonthByDept(dept));
        s.setApprovalNumerator(dao.countApprovalNumerator(dept));
        s.setApprovalDenominator(dao.countApprovalDenominator(dept));
        return s;
    }

    public List<Request> pending(String dept, LocalDate from, LocalDate to) throws SQLException {
        return dao.findPendingList(dept, from, to);
    }

    public List<Request> todayOff(String dept, LocalDate today) throws SQLException {
        return dao.findTodayOff(dept, today);
    }
}
