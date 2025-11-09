package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.SettingDAO;
import com.acme.leavemgmt.model.Setting;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.Csrf;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "AdminSettingsServlet", urlPatterns = {"/admin/settings"})
public class AdminSettingsServlet extends HttpServlet {

    private final SettingDAO settingDAO = new SettingDAO();

    /* ------------ helpers ------------ */

    private static User currentUser(HttpServletRequest req) {
        HttpSession ses = req.getSession(false);
        return (ses != null) ? (User) ses.getAttribute("currentUser") : null;
    }

    private static void deny(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.sendError(code, msg);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Không cache để tránh back/forward làm lệch token
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");

        User cur = currentUser(req);
        if (cur == null || !cur.isAdmin()) {
            deny(resp, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        try {
            List<Setting> settings = settingDAO.findAll();
            req.setAttribute("settings", settings);
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // Đẩy token xuống view (req attrs: csrfParam, csrfToken, csrfTokenObj, …)
        Csrf.protect(req);

        req.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User cur = currentUser(req);
        if (cur == null || !cur.isAdmin()) {
            deny(resp, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        // Nếu form là multipart mà bạn chưa dùng parser -> token sẽ không đọc được:
        // Khuyến nghị: chỉ dùng application/x-www-form-urlencoded ở trang này.
        String ct = req.getContentType();
        if (ct != null && ct.toLowerCase().startsWith("multipart/")) {
            deny(resp, HttpServletResponse.SC_BAD_REQUEST, "Use non-multipart form for settings");
            return;
        }

        // CSRF check (hỗ trợ cả input _csrf lẫn header X-CSRF-Token)
        if (!Csrf.isTokenValid(req)) {
            deny(resp, HttpServletResponse.SC_BAD_REQUEST, "CSRF invalid");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "save";

        try {
            if ("save".equalsIgnoreCase(action)) {
                // submit name="val_{id}"
                List<Setting> all = settingDAO.findAll();
                for (Setting s : all) {
                    String param = req.getParameter("val_" + s.getId());
                    if (param != null && !param.equals(s.getValue())) {
                        settingDAO.updateValue(s.getId(), param, cur.getId());
                        AuditLog.log(cur.getId(),
                                AuditLog.Event.ADMIN_SETTING_UPDATE,
                                "Cập nhật setting " + s.getKey() + " = " + param);
                    }
                }
            } else if ("create".equalsIgnoreCase(action)) {
                String key   = req.getParameter("new_key");
                String value = req.getParameter("new_value");
                String group = req.getParameter("new_group");
                String type  = req.getParameter("new_type");
                String desc  = req.getParameter("new_desc");
                if (key != null && !key.trim().isEmpty()) {
                    settingDAO.create(key.trim(), value, type, group, desc, cur.getId());
                    AuditLog.log(cur.getId(),
                            AuditLog.Event.ADMIN_SETTING_CREATE,
                            "Tạo setting " + key.trim());
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            // Ngăn replay: xoay token sau POST thành công/thất bại
            HttpSession s = req.getSession(false);
            if (s != null) Csrf.rotate(s);
        }

        // PRG
        resp.sendRedirect(req.getContextPath() + "/admin/settings?ok=1");
    }
}
