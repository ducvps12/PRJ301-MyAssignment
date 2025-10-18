package com.acme.leavemgmt.web.admin;

import com.acme.leavemgmt.util.DBConnection; // đổi path nếu khác
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="DbAdminServlet", urlPatterns={"/admin/db"})
public class DbAdminServlet extends HttpServlet {

    // Cho phép dùng GET hiển thị form, POST thực thi SELECT
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Kiểm tra session user role (ví dụ role="ADMIN")
        HttpSession s = req.getSession(false);
        if (s == null || !"ADMIN".equals(s.getAttribute("role"))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only admin can access");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/admin/dbviewer.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || !"ADMIN".equals(s.getAttribute("role"))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only admin can access");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String sql = req.getParameter("sql");
        if (sql == null) sql = "";

        sql = sql.trim();
        // Bắt buộc chỉ cho SELECT (ngăn mọi INSERT/UPDATE/DELETE/DDL)
        if (sql.isEmpty() || !sql.toLowerCase().startsWith("select")) {
            req.setAttribute("error", "Chỉ cho phép câu lệnh SELECT để xem dữ liệu.");
            req.getRequestDispatcher("/WEB-INF/views/admin/dbviewer.jsp").forward(req, resp);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            List<String> headers = new ArrayList<>();
            for (int i = 1; i <= cols; i++) headers.add(md.getColumnLabel(i));

            List<List<String>> rows = new ArrayList<>();
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= cols; i++) {
                    String val = rs.getString(i);
                    row.add(val == null ? "" : val);
                }
                rows.add(row);
            }

            req.setAttribute("headers", headers);
            req.setAttribute("rows", rows);
            req.setAttribute("sql", sql);

            req.getRequestDispatcher("/WEB-INF/views/admin/dbviewer.jsp").forward(req, resp);

        } catch (SQLException ex) {
            req.setAttribute("error", "SQL error: " + ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/admin/dbviewer.jsp").forward(req, resp);
        }
    }
}
