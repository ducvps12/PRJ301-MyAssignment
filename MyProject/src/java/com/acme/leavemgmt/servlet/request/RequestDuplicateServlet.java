package com.acme.leavemgmt.servlet.request;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;

@WebServlet(urlPatterns = {"/request/duplicate"})
public class RequestDuplicateServlet extends HttpServlet {

    private final RequestDAO dao = new RequestDAO();

    private static String up(String s){ return s==null? "" : s.trim().toUpperCase(); }
    private static Integer uid(User u){
        if (u == null) return null;
        try { Integer v = u.getUserId(); if (v != null) return v; } catch(Exception ignore){}
        try { Integer v = u.getId();     if (v != null) return v; } catch(Exception ignore){}
        return null;
    }
    private static boolean adminLike(User u){
        String r1 = up(u==null?null:u.getRole());
        String r2 = up(u==null?null:u.getRoleCode());
        return r1.matches("ADMIN|SYS_ADMIN|HR|HR_ADMIN|DIV_LEADER|TEAM_LEAD|MANAGER")
            || r2.matches("ADMIN|SYS_ADMIN|HR|HR_ADMIN|DIV_LEADER|TEAM_LEAD|MANAGER");
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // user
        HttpSession s = req.getSession(false);
        User me = (s!=null && s.getAttribute("currentUser") instanceof User)
                ? (User) s.getAttribute("currentUser")
                : (s!=null && s.getAttribute("user") instanceof User)
                    ? (User) s.getAttribute("user") : null;
        Integer meId = uid(me);
        if (meId == null){ resp.sendError(401, "Please login"); return; }

        // id nguồn
        String idStr = req.getParameter("id");
        if (idStr==null || !idStr.matches("\\d+")) { resp.sendError(400,"Invalid id"); return; }
        int srcId = Integer.parseInt(idStr);

        try {
            Request src = dao.findById(srcId);
            if (src == null){ resp.sendError(404, "Request not found"); return; }

            boolean isOwner = Objects.equals(src.getUserId(), meId);
            boolean can = isOwner || adminLike(me);
            if (!can){ resp.sendError(403, "Not allowed"); return; }

            int newId = dao.duplicateRequest(srcId, meId); // <— tạo đơn mới

            String msg = URLEncoder.encode("Đã nhân bản từ #" + srcId, StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath()+"/request/detail?id="+newId+"&msg="+msg);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
