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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // check quyền giống các servlet admin khác
        HttpSession ses = req.getSession(false);
        User current = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        if (current == null || !current.isAdmin()) {   // sửa theo hàm của bạn
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            List<Setting> settings = settingDAO.findAll();
            req.setAttribute("settings", settings);
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // csrf
        Csrf.addToken(req);

        req.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        User current = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
        if (current == null || !current.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // check csrf
        if (!Csrf.isTokenValid(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "CSRF invalid");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "save";

        try {
            if ("save".equals(action)) {
                // cách submit: name="val_{id}"
                List<Setting> all = settingDAO.findAll();
                for (Setting s : all) {
                    String param = req.getParameter("val_" + s.getId());
                    if (param != null && !param.equals(s.getValue())) {
                        settingDAO.updateValue(s.getId(), param, current.getId());
                        AuditLog.log(current.getId(),
                                     AuditLog.Event.ADMIN_SETTING_UPDATE,
                                     "Cập nhật setting " + s.getKey() + " = " + param);
                    }
                }
            } else if ("create".equals(action)) {
                String key = req.getParameter("new_key");
                String value = req.getParameter("new_value");
                String group = req.getParameter("new_group");
                String type  = req.getParameter("new_type");
                String desc  = req.getParameter("new_desc");
                if (key != null && !key.isEmpty()) {
                    settingDAO.create(key.trim(), value, type, group, desc, current.getId());
                    AuditLog.log(current.getId(),
                                 AuditLog.Event.ADMIN_SETTING_CREATE,
                                 "Tạo setting " + key);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/settings?ok=1");
    }
}
