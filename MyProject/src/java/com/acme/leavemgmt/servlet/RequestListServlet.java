package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.RequestDAO;
import com.acme.leavemgmt.model.Request;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "RequestListServlet", urlPatterns = {"/request/list"})
public class RequestListServlet extends HttpServlet {
    private final RequestDAO dao = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) s.getAttribute("userId");
        String role = (String) s.getAttribute("role");          // ví dụ: ADMIN / MANAGER / EMPLOYEE
        String dept = (String) s.getAttribute("department");    // ví dụ: IT / QA / Sale

        String mineParam = req.getParameter("mine");            // "1" -> chỉ của tôi

        try {
            List<Request> requests = new ArrayList<>();

            // luôn có đơn của chính mình
            List<Request> mine = dao.listMyRequests(userId);

            if ("1".equals(mineParam) || "EMPLOYEE".equalsIgnoreCase(role)) {
                requests = mine; // chỉ của tôi, hoặc nhân viên thường
            } else {
                // manager/leader: cộng thêm đơn cấp dưới theo phòng ban
                List<Request> subs = dao.listSubordinateRequests(dept);
                requests.addAll(mine);
                requests.addAll(subs);
            }

            req.setAttribute("requests", requests);
req.getRequestDispatcher("/WEB-INF/views/request/list.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
