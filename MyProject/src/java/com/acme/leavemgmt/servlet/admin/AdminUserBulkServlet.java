package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/**
 * Xử lý bulk operations cho Users: activate, deactivate, resetpw
 */
@WebServlet(name = "AdminUserBulkServlet", urlPatterns = {"/admin/users/bulk"})
public class AdminUserBulkServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        User me = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        if (me == null || !(me.isAdmin() || me.isLeader())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!Csrf.verifyToken(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "CSRF token không hợp lệ");
            return;
        }

        // Lấy danh sách IDs
        List<Integer> ids = new ArrayList<>();
        String[] bulkIds = req.getParameterValues("ids");
        if (bulkIds != null) {
            for (String id : bulkIds) {
                if (id != null && !id.isBlank()) {
                    try {
                        ids.add(Integer.parseInt(id.trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (ids.isEmpty()) {
            setFlashMessage(ses, "error", "Chưa chọn người dùng nào");
            redirectBack(req, resp);
            return;
        }

        String action = n(req.getParameter("action"));
        if (action.isEmpty()) {
            setFlashMessage(ses, "error", "Chưa chọn hành động");
            redirectBack(req, resp);
            return;
        }

        int updated = 0;
        String actionName = "";
        String message = "";

        try (Connection cn = DBConnection.getConnection()) {
            switch (action.toLowerCase()) {
                case "activate":
                    updated = bulkUpdateStatus(cn, ids, "ACTIVE");
                    actionName = "Kích hoạt";
                    message = "Đã kích hoạt " + updated + " tài khoản";
                    break;
                case "deactivate":
                    updated = bulkUpdateStatus(cn, ids, "INACTIVE");
                    actionName = "Vô hiệu hóa";
                    message = "Đã vô hiệu hóa " + updated + " tài khoản";
                    break;
                case "resetpw":
                    updated = bulkResetPassword(cn, ids);
                    actionName = "Reset mật khẩu";
                    message = "Đã reset mật khẩu " + updated + " tài khoản về \"123456\"";
                    break;
                default:
                    setFlashMessage(ses, "error", "Hành động không hợp lệ: " + action);
                    redirectBack(req, resp);
                    return;
            }

            // Audit log
            try {
                AuditLog.log(req, "ADMIN_USERS_BULK_" + action.toUpperCase(), "USER", me.getId(),
                        actionName + " " + ids.size() + " users: " + ids);
            } catch (Throwable ignored) {}

            setFlashMessage(ses, "success", message);
        } catch (SQLException e) {
            throw new ServletException("Lỗi khi thực hiện bulk operation", e);
        }

        redirectBack(req, resp);
    }

    private int bulkUpdateStatus(Connection cn, List<Integer> ids, String status) throws SQLException {
        if (ids.isEmpty()) return 0;
        
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "UPDATE dbo.Users SET status = ? WHERE id IN (" + placeholders + ")";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, status);
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 2, ids.get(i));
            }
            return ps.executeUpdate();
        }
    }

    private int bulkResetPassword(Connection cn, List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) return 0;
        
        String defaultPw = "123456"; // TODO: hash nếu có util
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "UPDATE dbo.Users SET password = ? WHERE id IN (" + placeholders + ")";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, defaultPw);
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 2, ids.get(i));
            }
            return ps.executeUpdate();
        }
    }

    private void redirectBack(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseUrl = req.getContextPath() + "/admin/users";
        StringBuilder url = new StringBuilder(baseUrl);
        
        String q = n(req.getParameter("q"));
        String status = n(req.getParameter("status"));
        String page = n(req.getParameter("page"));
        String size = n(req.getParameter("size"));
        
        List<String> params = new ArrayList<>();
        if (!page.isEmpty()) params.add("page=" + page);
        if (!size.isEmpty()) params.add("size=" + size);
        if (!q.isEmpty()) params.add("q=" + URLEncoder.encode(q, StandardCharsets.UTF_8));
        if (!status.isEmpty()) params.add("status=" + status);
        
        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }
        
        resp.sendRedirect(url.toString());
    }

    private void setFlashMessage(HttpSession ses, String type, String message) {
        if (ses != null) {
            ses.setAttribute("flash_" + type, message);
        }
    }

    private static String n(String s) {
        return (s == null) ? "" : s.trim();
    }
}








