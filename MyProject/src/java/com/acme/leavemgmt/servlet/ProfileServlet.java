package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

public class ProfileServlet extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  // ===== GET: hiển thị hồ sơ =====
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User cu = (User) req.getSession().getAttribute("currentUser");
    try {
      if (cu == null) {
        // Cho xem read-only dạng Guest (nếu muốn ép login, thay bằng redirect tới /login?next=/profile)
        User guest = new User();
        guest.setFullName("Guest"); guest.setDepartment("—"); guest.setRole("—");
        req.setAttribute("me", guest);
        req.setAttribute("canEdit", false);
      } else {
        // luôn lấy mới từ DB để tránh dữ liệu cũ trong session
        User me = userDAO.findById(cu.getId());
        req.setAttribute("me", me);
        req.setAttribute("canEdit", true);
      }

      // flash message (nếu có)
      Object ok = req.getSession().getAttribute("flash_ok");
      Object err = req.getSession().getAttribute("flash_err");
      if (ok != null) { req.setAttribute("ok", ok); req.getSession().removeAttribute("flash_ok"); }
      if (err != null){ req.setAttribute("error", err); req.getSession().removeAttribute("flash_err"); }

      req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  // ===== POST: cập nhật hồ sơ =====
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User cu = (User) req.getSession().getAttribute("currentUser");
    if (cu == null) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

    try {
      req.setCharacterEncoding("UTF-8");

      // Lấy input
      String fullName   = trim(req.getParameter("fullName"));
      String department = trim(req.getParameter("department"));
      String roleInput  = trim(req.getParameter("role"));       // sẽ kiểm soát bên dưới
      String email      = trim(req.getParameter("email"));
      String phone      = trim(req.getParameter("phone"));
      String address    = trim(req.getParameter("address"));
      String birthdayS  = trim(req.getParameter("birthday"));   // yyyy-MM-dd hoặc rỗng
      String bio        = trim(req.getParameter("bio"));
      String avatarUrl  = trim(req.getParameter("avatarUrl"));

      // Validate nhẹ
      if (email != null && !email.isBlank() &&
          !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
        flashErr(req, "Email không hợp lệ");
        resp.sendRedirect(req.getContextPath()+"/profile");
        return;
      }
      if (phone != null && !phone.isBlank() &&
          !phone.matches("^[0-9+()\\-\\s]{6,20}$")) {
        flashErr(req, "Số điện thoại không hợp lệ");
        resp.sendRedirect(req.getContextPath()+"/profile");
        return;
      }

      // Parse birthday (nullable)
      LocalDate birthday = null;
      if (birthdayS != null && !birthdayS.isBlank()) {
        try { birthday = LocalDate.parse(birthdayS); }
        catch (Exception ignore) {
          flashErr(req, "Định dạng ngày sinh phải là yyyy-MM-dd");
          resp.sendRedirect(req.getContextPath()+"/profile");
          return;
        }
      }

      // Chống leo quyền: chỉ ADMIN mới được thay đổi role
      String roleToSave;
      if (cu.getRole() != null && cu.getRole().equalsIgnoreCase("ADMIN")) {
        roleToSave = roleInput; // admin được thay
      } else {
        // giữ nguyên role hiện tại trong DB
        User meNow = userDAO.findById(cu.getId());
        roleToSave = meNow != null ? meNow.getRole() : null;
      }

      // Gọi DAO cập nhật
      boolean ok = userDAO.updateProfile(
          cu.getId(),
          nullIfBlank(fullName),
          nullIfBlank(department),
          nullIfBlank(roleToSave),
          nullIfBlank(email),
          nullIfBlank(phone),
          nullIfBlank(address),
          birthday,                 // LocalDate
          nullIfBlank(bio),
          nullIfBlank(avatarUrl)
      );

      if (ok) {
        // đồng bộ tên hiển thị ở header (nếu bạn dùng)
        if (fullName != null && !fullName.isBlank()) {
          cu.setFullName(fullName);
          req.getSession().setAttribute("currentUser", cu);
        }
        flashOk(req, "Cập nhật thành công!");
      } else {
        flashErr(req, "Cập nhật thất bại. Vui lòng thử lại.");
      }

      // PRG
      resp.sendRedirect(req.getContextPath()+"/profile");
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  // ===== helpers =====
  private static String trim(String s){ return s == null ? null : s.trim(); }
  private static String nullIfBlank(String s){ return (s == null || s.isBlank()) ? null : s; }
  private static void flashOk(HttpServletRequest r, String msg){ r.getSession().setAttribute("flash_ok", msg); }
  private static void flashErr(HttpServletRequest r, String msg){ r.getSession().setAttribute("flash_err", msg); }
}
