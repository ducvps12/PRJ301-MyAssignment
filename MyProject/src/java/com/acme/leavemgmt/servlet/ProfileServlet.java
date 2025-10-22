package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Nếu có user -> show đúng hồ sơ của họ
        User cu = (User) req.getSession().getAttribute("currentUser");
        try {
            if (cu != null) {
                req.setAttribute("me", userDAO.findById(cu.getId()));
                req.setAttribute("canEdit", true);           // đang đăng nhập -> cho sửa
            } else {
                // Guest: hiển thị profile rỗng/đọc-only
                User guest = new User();
                guest.setFullName("Guest");
                guest.setDepartment("—");
                guest.setRole("—");
                req.setAttribute("me", guest);
                req.setAttribute("canEdit", false);          // guest -> không sửa
            }
            req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Chỉ cho phép sửa khi đã đăng nhập
        User cu = (User) req.getSession().getAttribute("currentUser");
        if (cu == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Please login to update profile.");
            return;
        }
        try {
            User u = new User();
            u.setId(cu.getId());
            u.setFullName(req.getParameter("fullName"));
            u.setDepartment(req.getParameter("department"));
            u.setRole(req.getParameter("role"));

            userDAO.updateBasic(u); // UPDATE Users SET full_name=?, department=?, role=? WHERE id=?

            resp.sendRedirect(req.getContextPath() + "/profile?ok=1");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
