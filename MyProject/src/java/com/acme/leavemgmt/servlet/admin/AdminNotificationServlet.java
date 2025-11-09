package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.NotificationDAO;
import com.acme.leavemgmt.dao.NotificationDAO.Row;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/admin/notifications"})
public class AdminNotificationServlet extends HttpServlet {

    private NotificationDAO dao;

    @Override public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
        Object dsObj = cfg.getServletContext().getAttribute("DS");
        if (!(dsObj instanceof DataSource)) {
            throw new ServletException("Missing DataSource in ServletContext attr 'DS'");
        }
        dao = new NotificationDAO((DataSource) dsObj);
    }

    private User current(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s != null) ? (User) s.getAttribute("currentUser") : null;
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = current(req);
        if (u == null || !u.isAdmin()) { resp.sendError(403); return; }

        try {
            List<Row> items = dao.listAll();
            req.setAttribute("items", items);
        } catch (SQLException e) {
            throw new ServletException("Load notifications error", e);
        }

        Csrf.protect(req);
        req.getRequestDispatcher("/WEB-INF/views/admin/notifications.jsp").forward(req, resp);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = current(req);
        if (u == null || !u.isAdmin()) { resp.sendError(403); return; }
        if (!Csrf.isTokenValid(req))   { resp.sendError(400, "CSRF invalid"); return; }

        String action = Optional.ofNullable(req.getParameter("action")).orElse("create");
        try {
            switch (action) {
                case "create": {
                    String title = req.getParameter("title");
                    String body  = req.getParameter("content");
                    String link  = req.getParameter("link_url");
                    if (title != null && !title.isBlank()) {
                        // FIX: lưu id của admin hiện tại để tránh NULL user_id
                        dao.create(u.getId(), title.trim(),
                                   body == null ? "" : body.trim(),
                                   (link == null || link.isBlank()) ? null : link.trim());
                    }
                    break;
                }
                case "mark": {
                    int id = Integer.parseInt(req.getParameter("id"));
                    boolean read = "1".equals(req.getParameter("read"));
                    dao.setRead(id, read);
                    break;
                }
                case "delete": {
                    int id = Integer.parseInt(req.getParameter("id"));
                    dao.delete(id);
                    break;
                }
                default: /* no-op */ ;
            }
        } catch (SQLException e) {
            throw new ServletException("Write notifications error", e);
        }

        Csrf.rotate(req.getSession(false));
        resp.sendRedirect(req.getContextPath() + "/admin/notifications?ok=1");
    }
}
