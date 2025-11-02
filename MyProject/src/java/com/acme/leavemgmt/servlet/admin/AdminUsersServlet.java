package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Trang danh sách người dùng
 * URL: /admin/users
 */
@WebServlet(name = "AdminUsersServlet", urlPatterns = {"/admin/users"})
public class AdminUsersServlet extends HttpServlet {

    /** ========== DTO dùng cho bảng JSP ========== */
    public static class Row {
        private int id;
        private String username, fullName, email, role, department, status;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getDepartment() { return department; }
        public String getStatus() { return status; }

        public void setId(int id) { this.id = id; }
        public void setUsername(String username) { this.username = username; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setEmail(String email) { this.email = email; }
        public void setRole(String role) { this.role = role; }
        public void setDepartment(String department) { this.department = department; }
        public void setStatus(String status) { this.status = status; }
    }

    /** ========== Object phân trang ========== */
    public static class Page<T> {
        private int pageIndex, pageSize, totalPages, totalItems;
        private List<T> data = new ArrayList<>();

        public int getPageIndex() { return pageIndex; }
        public int getPageSize() { return pageSize; }
        public int getTotalPages() { return totalPages; }
        public int getTotalItems() { return totalItems; }
        public List<T> getData() { return data; }

        public boolean isHasPrev() { return pageIndex > 1; }
        public boolean isHasNext() { return pageIndex < totalPages; }
        public int getPrevPage() { return Math.max(1, pageIndex - 1); }
        public int getNextPage() { return Math.min(totalPages, pageIndex + 1); }

        public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public void setData(List<T> data) { this.data = data; }
    }

    /** ========== GET: Danh sách người dùng ========== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Kiểm tra đăng nhập + quyền
        HttpSession ses = req.getSession(false);
        User me = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        if (me == null || !(me.isAdmin() || me.isLeader())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2. Lấy filter & paging
        String q = n(req.getParameter("q"));
        String fStatus = n(req.getParameter("status"));
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 10);
        if (size <= 0 || size > 100) size = 10;
        if (page <= 0) page = 1;
        int offset = (page - 1) * size;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (!q.isEmpty()) {
            where.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?) ");
            String kw = "%" + q + "%";
            params.add(kw); params.add(kw); params.add(kw);
        }
        if (fStatus.equalsIgnoreCase("ACTIVE")) {
            where.append(" AND status = 'ACTIVE' ");
        } else if (fStatus.equalsIgnoreCase("INACTIVE")) {
            where.append(" AND status = 'INACTIVE' ");
        }

        Page<Row> pageObj = new Page<>();
        pageObj.setPageIndex(page);
        pageObj.setPageSize(size);

        // 3. Truy vấn DB
        try (Connection cn = DBConnection.getConnection()) {
            // --- COUNT ---
            try (PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM dbo.Users " + where)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pageObj.setTotalItems(rs.getInt(1));
                    }
                }
            }

            int totalPages = (int) Math.ceil(pageObj.getTotalItems() / (double) size);
            if (totalPages < 1) totalPages = 1;
            if (page > totalPages) page = totalPages;
            offset = (page - 1) * size;
            pageObj.setTotalPages(totalPages);
            pageObj.setPageIndex(page);

            // --- DATA ---
            String sqlData = """
                SELECT id, username, full_name AS fullName, email, role, department, status
                FROM dbo.Users
                """ + where + " ORDER BY id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            List<Object> p2 = new ArrayList<>(params);
            p2.add(offset);
            p2.add(size);

            List<Row> rows = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(sqlData)) {
                bind(ps, p2);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Row r = new Row();
                        r.setId(rs.getInt("id"));
                        r.setUsername(rs.getString("username"));
                        r.setFullName(rs.getString("fullName"));
                        r.setEmail(rs.getString("email"));
                        r.setRole(rs.getString("role"));
                        r.setDepartment(rs.getString("department"));
                        r.setStatus(rs.getString("status"));
                        rows.add(r);
                    }
                }
            }
            pageObj.setData(rows);

        } catch (SQLException e) {
            throw new ServletException("Lỗi khi truy vấn danh sách người dùng", e);
        }

        // 4. Đổ dữ liệu ra JSP
        req.setAttribute("csrf", Csrf.ensureToken(req.getSession()));
        req.setAttribute("page", pageObj);
        req.setAttribute("q", q);
        req.setAttribute("status", fStatus);

        // --- Hiển thị thông báo flash nếu có ---
        if (ses != null) {
            Object flash = ses.getAttribute("flash_success");
            if (flash != null) {
                req.setAttribute("flash_success", flash);
                ses.removeAttribute("flash_success");
            }
        }

        resp.setHeader("Cache-Control", "no-store");
        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    /* ====== Helper ====== */
    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v instanceof Integer) ps.setInt(i + 1, (Integer) v);
            else ps.setString(i + 1, String.valueOf(v));
        }
    }

    private static String n(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
