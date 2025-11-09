package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(urlPatterns = {"/request/detail", "/request/detail/*"})
public class RequestDetailServlet extends HttpServlet {

    private final RequestDAO dao = new RequestDAO();

    /* ===== Utils (null-safe, hỗ trợ đa model User) ===== */
    private static String up(String s){ return s==null? "": s.trim().toUpperCase(); }
    private static String callStr(Object o, String m){
        if (o==null) return null;
        try { Object v = o.getClass().getMethod(m).invoke(o); return v==null? null : String.valueOf(v); }
        catch (Exception ignore){ return null; }
    }
    private static Integer callInt(Object o, String m){
        if (o==null) return null;
        try { Object v = o.getClass().getMethod(m).invoke(o); return (v==null)? null : (Integer)v; }
        catch (Exception ignore){ return null; }
    }
    private static boolean hasAnyRole(User u, String... allowed){
        if (u == null) return false;
        String r1 = up(callStr(u, "getRole"));
        String r2 = up(callStr(u, "getRoleCode"));
        for (String a: allowed){
            String A = up(a);
            if (A.equals(r1) || A.equals(r2)) return true;
        }
        return false;
    }
    private static Integer uid(User u){
        Integer v = callInt(u, "getUserId");
        if (v != null) return v;
        return callInt(u, "getId");
    }

    /** Đọc id từ query (?id=) hoặc path (/request/detail/{id}) */
    private Integer readId(HttpServletRequest req) {
        String id = req.getParameter("id");
        if (id == null) {
            String pi = req.getPathInfo();   // ví dụ "/21"
            if (pi != null && pi.length() > 1) id = pi.substring(1);
        }
        try { return (id != null && id.matches("\\d+")) ? Integer.valueOf(id) : null; }
        catch (Exception ignore){ return null; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Charset
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        // Current user: currentUser -> user
        HttpSession ses = req.getSession(false);
        User me = null;
        if (ses != null) {
            Object u = ses.getAttribute("currentUser");
            if (u == null) u = ses.getAttribute("user");
            if (u instanceof User) me = (User) u;
        }
        req.setAttribute("me", me);

        // ID
        Integer id = readId(req);
        if (id == null) { resp.sendError(400, "Invalid id"); return; }

        try {
            // Data
            Request r = dao.findById(id);
            if (r == null) { resp.sendError(404, "Request not found"); return; }

            // Quyền xem
            Integer meId = uid(me);
            boolean isOwner    = meId != null && Objects.equals(r.getUserId(), meId);
            boolean isAdminLike = hasAnyRole(me,
                    "ADMIN","SYS_ADMIN","HR","HR_ADMIN","DIV_LEADER","TEAM_LEAD","MANAGER");

            boolean canView = isOwner || isAdminLike || (r.getUserId()==null && isAdminLike);

            // Dev bypass (?dev=1)
            if (!canView && "1".equals(req.getParameter("dev"))) canView = true;

            if (!canView) {
                // tránh trắng trang: trả HTML ngắn gọn
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("""
                    <!doctype html><meta charset='utf-8'>
                    <style>body{font-family:system-ui;padding:24px}</style>
                    <h2>403 - Bạn không có quyền xem yêu cầu này</h2>
                    <p><a href='javascript:history.back()'>← Quay lại</a></p>
                """);
                return;
            }

            // History/attachments rỗng nếu chưa kết nối DAO phụ
            List<?> history = Collections.emptyList();
            List<Map<String,Object>> attachments = Collections.emptyList();

            // Attributes cho JSP
            req.setAttribute("r", r);
            req.setAttribute("history", history);
            req.setAttribute("attachments", attachments);

            boolean canCancel  = isOwner && "PENDING".equalsIgnoreCase(r.getStatus());
            boolean canApprove = isAdminLike && "PENDING".equalsIgnoreCase(r.getStatus());
            req.setAttribute("canCancel",  canCancel);
            req.setAttribute("canApprove", canApprove);

            // Forward
            req.getRequestDispatcher("/WEB-INF/views/request/detail.jsp").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
