package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.util.Csrf;
import com.acme.leavemgmt.util.Servlets;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RequestBulkServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!Servlets.isManager(req)) { resp.sendError(403); return; }
        if (!Csrf.valid(req)) { resp.sendError(400, "Invalid CSRF"); return; }

        String action = Servlets.trim(req.getParameter("action")); // approve|reject|cancel
        String note   = Servlets.trim(req.getParameter("note"));
        String[] ids  = req.getParameterValues("ids");

        if (ids == null || ids.length==0 || action.isEmpty()){
            req.getSession().setAttribute("flash", "Chưa chọn bản ghi hoặc hành động.");
            resp.sendRedirect(req.getContextPath()+"/request/list");
            return;
        }

        List<Integer> idList = Arrays.stream(ids).map(Integer::parseInt).collect(Collectors.toList());
        try{
            int n = dao.bulkUpdate(idList, action, Servlets.currentUserId(req), note);
            req.getSession().setAttribute("flash", "Đã xử lý: "+n+" bản ghi.");
            resp.sendRedirect(req.getContextPath()+"/request/list");
        }catch(Exception e){
            throw new ServletException(e);
        }
    }
}
