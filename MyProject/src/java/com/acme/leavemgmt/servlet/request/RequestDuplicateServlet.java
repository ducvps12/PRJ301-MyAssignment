package com.acme.leavemgmt.servlet.request;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import com.acme.leavemgmt.util.Servlets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/request/duplicate")
public class RequestDuplicateServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            
            Integer srcId = Servlets.safeInt(req.getParameter("id"));
            if (srcId == null) { resp.sendError(400, "Missing id"); return; }
            
            int meId = Servlets.currentUserId(req);
            String meName = Servlets.currentUserFullname(req);
            
            Request src = dao.findById(srcId);
            if (src == null) { resp.sendError(404, "Not found"); return; }
            
            // Chỉ cho nhân bản nếu là chủ đơn hoặc là cấp quản lý
            if (src.getUserId() != meId && !Servlets.isManager(req)) {
                resp.sendError(403); return;
            }
            
            try {
                int newId = dao.duplicateFrom(srcId, meId, meName,
                        /*copyAttachments*/ false,
                        /*overrideReason*/ null,
                        /*overrideStart*/ null,
                        /*overrideEnd*/ null,
                        /*overrideLeaveTypeId*/ null);
                
                req.getSession().setAttribute("flash",
                        "Đã nhân bản đơn #" + srcId + " → #" + newId);
                resp.sendRedirect(req.getContextPath()+"/request/detail?id="+newId);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestDuplicateServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // nếu muốn submit với ngày/ghi chú mới… thì cho POST, parse params rồi gọi dao.duplicateFrom(...) tương tự
}
