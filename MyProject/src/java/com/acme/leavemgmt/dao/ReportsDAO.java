package com.acme.leavemgmt.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class ReportsDAO {
  private final Connection cnn;
  public ReportsDAO(Connection cnn){ this.cnn = cnn; }

  // Đếm request theo ngày (YYYY-MM-DD)
  public LinkedHashMap<LocalDate,Integer> requestsDaily(LocalDate from, LocalDate to, Integer deptId, String status) throws SQLException {
    StringBuilder sql = new StringBuilder(
      "SELECT CAST(r.start_date AS date) AS d, COUNT(*) AS c " +
      "FROM Requests r " +
      "JOIN Users u ON u.id = r.user_id " +
      "WHERE r.start_date BETWEEN ? AND ? "
    );
    if (status != null) sql.append("AND r.status = ? ");
    if (deptId != null) sql.append("AND u.department_id = ? ");
    sql.append("GROUP BY CAST(r.start_date AS date) ORDER BY d");

    LinkedHashMap<LocalDate,Integer> out = new LinkedHashMap<>();
    try (PreparedStatement ps = cnn.prepareStatement(sql.toString())) {
      int i=1;
      ps.setDate(i++, Date.valueOf(from));
      ps.setDate(i++, Date.valueOf(to));
      if (status != null)  ps.setString(i++, status);
      if (deptId != null)  ps.setInt(i++, deptId);
      try (ResultSet rs = ps.executeQuery()){
        while (rs.next()){
          out.put(rs.getDate("d").toLocalDate(), rs.getInt("c"));
        }
      }
    }
    return out;
  }

  // Tổng theo phòng ban (mã/phòng)
  public LinkedHashMap<String,Integer> requestsByDept(LocalDate from, LocalDate to) throws SQLException {
    String sql =
      "SELECT COALESCE(u.department, 'N/A') AS dept, COUNT(*) AS c " +
      "FROM Requests r JOIN Users u ON u.id = r.user_id " +
      "WHERE r.start_date BETWEEN ? AND ? " +
      "GROUP BY u.department ORDER BY c DESC";
    LinkedHashMap<String,Integer> out = new LinkedHashMap<>();
    try (PreparedStatement ps = cnn.prepareStatement(sql)) {
      ps.setDate(1, Date.valueOf(from));
      ps.setDate(2, Date.valueOf(to));
      try (ResultSet rs = ps.executeQuery()){
        while (rs.next()) out.put(rs.getString("dept"), rs.getInt("c"));
      }
    }
    return out;
  }

  // Tổng theo trạng thái (pending/approved/rejected/cancelled)
  public LinkedHashMap<String,Integer> requestsByStatus(LocalDate from, LocalDate to) throws SQLException {
    String sql =
      "SELECT r.status, COUNT(*) AS c " +
      "FROM Requests r " +
      "WHERE r.start_date BETWEEN ? AND ? " +
      "GROUP BY r.status ORDER BY c DESC";
    LinkedHashMap<String,Integer> out = new LinkedHashMap<>();
    try (PreparedStatement ps = cnn.prepareStatement(sql)) {
      ps.setDate(1, Date.valueOf(from));
      ps.setDate(2, Date.valueOf(to));
      try (ResultSet rs = ps.executeQuery()){
        while (rs.next()) out.put(rs.getString("status"), rs.getInt("c"));
      }
    }
    return out;
  }
}
