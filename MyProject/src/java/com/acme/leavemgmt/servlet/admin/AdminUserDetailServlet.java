package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "AdminUserDetailServlet", urlPatterns = {"/admin/users/detail"})
public class AdminUserDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer id = parseInt(req.getParameter("id"));
        if (id == null || id <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid id");
            return;
        }

        String sql = "SELECT id, username, full_name, email, role, department, status " +
                     "FROM Users WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                    return;
                }

                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));

                // 2 cột này trong model đang là String
                u.setRole(rs.getString("role"));               // STAFF / DIV_LEADER ...
                u.setDepartment(rs.getString("department"));   // IT / QA / SALE ...

                // DB đang để text, model lại muốn int -> map sang int
                String st = rs.getString("status");            // ACTIVE / INACTIVE / 1 / 0
                int statusVal = normalizeStatus(st);
                u.setStatus(statusVal);

                req.setAttribute("u", u);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // forward tới JSP
        req.getRequestDispatcher("/WEB-INF/views/admin/user_detail.jsp")
           .forward(req, resp);
    }

    private Integer parseInt(String s) {
        try {
            return (s == null) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Chuyển mọi kiểu status trong DB về int cho hợp với model
     */
    private int normalizeStatus(String raw) {
        if (raw == null) return 0;
        raw = raw.trim();
        // nếu DB để chữ
        if ("ACTIVE".equalsIgnoreCase(raw)) return 1;
        if ("INACTIVE".equalsIgnoreCase(raw)) return 0;
        // nếu DB để số string thì convert
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
