package com.acme.leavemgmt.servlet;

import com.acme.leavemgmt.dao.DeptDAO;
import com.acme.leavemgmt.dao.DivisionDAO;
import com.acme.leavemgmt.dao.RoleDAO;
import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  // ===== DataSource detection =====
  @Resource(name = "jdbc/LeaveDB")
  private DataSource injectedDs;

  private DataSource tryResolveDs() {
    try {
      if (injectedDs != null) return injectedDs;
      Object v = getServletContext().getAttribute("DS");
      if (v instanceof DataSource) return (DataSource) v;
      try {
        return (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
      } catch (NamingException ignore) {}
    } catch (Exception ignore) {}
    return null;
  }

  // ===== DAO =====
  private UserDAO userDAO;
  private DeptDAO deptDAO;
  private DivisionDAO divisionDAO;
  private RoleDAO roleDAO;

  // ===== lifecycle =====
 @Override
public void init() throws ServletException {
  DataSource ds = tryResolveDs();
  if (ds == null) {
    // Fallback: DataSource “mỏng” dùng DBConnection.getConnection()
    ds = new com.acme.leavemgmt.util.SimpleDataSource();
  }

  try {
    userDAO     = new UserDAO(ds);
    deptDAO     = new DeptDAO(ds);
    divisionDAO = new DivisionDAO(ds);
    roleDAO     = new RoleDAO(ds);
  } catch (Exception e) {
    throw new ServletException("Init DAOs failed", e);
  }
}

  // ===== GET =====
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

    HttpSession ses = req.getSession(false);
    User sessionUser = (ses != null) ? (User) ses.getAttribute("currentUser") : null;

    try {
      User me;
      boolean canEditSelf;

      if (sessionUser == null) {
        me = new User();
        me.setFullName("Guest");
        canEditSelf = false;
      } else {
        me = userDAO.findById(sessionUser.getId());
        if (me == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
        canEditSelf = true;
      }

      // Lists
      req.setAttribute("depts",     safeList(deptDAO.listAll()));
      req.setAttribute("divisions", safeList(divisionDAO.listAll()));
      req.setAttribute("roles",     safeList(roleDAO.listAll()));
      req.setAttribute("managers",  safeList(userDAO.listManagers()));

      // birthday string
      if (me.getBirthday() != null) {
        req.setAttribute("uBirthdayStr", me.getBirthday().toString());
      }
      try {
        if (me.getCreatedAtDate() != null) req.setAttribute("uCreatedAtDate", me.getCreatedAtDate());
      } catch (Throwable ignore) {}

      // position label
      String divisionName   = nz(me.getDivisionName());
      String departmentName = nz(me.getDepartmentName());
      String roleName       = !nz(me.getRoleName()).isEmpty() ? me.getRoleName() : nz(me.getRole());
      String managerName    = nz(me.getManagerName());

      String pos = join(" › ", nonEmpty(divisionName), nonEmpty(departmentName));
      if (!roleName.isEmpty())    pos = pos.isEmpty() ? roleName : pos + " · " + roleName;
      if (!managerName.isEmpty()) pos += " · Manager: " + managerName;
      req.setAttribute("positionLabel", pos);

      // bind
      req.setAttribute("me", me);
      req.setAttribute("canEdit", canEditSelf);

      // flash
      moveFlash(req, "flash_ok",  "ok");
      moveFlash(req, "flash_err", "error");

      req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    } catch (Exception e) {
      log("ProfileServlet GET error", e);
      throw new ServletException("Không tải được hồ sơ người dùng", e);
    }
  }

  // ===== POST =====
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

    HttpSession ses = req.getSession(false);
    User cu = (ses != null) ? (User) ses.getAttribute("currentUser") : null;
    if (cu == null) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

    try {
      String fullName  = trim(req.getParameter("fullName"));
      String email     = trim(req.getParameter("email"));
      String phone     = trim(req.getParameter("phone"));
      String address   = trim(req.getParameter("address"));
      String birthdayS = trim(req.getParameter("birthday"));
      String bio       = trim(req.getParameter("bio"));
      String avatarUrl = trim(req.getParameter("avatarUrl"));

      // FK – chỉ admin được xét; nếu không => bỏ qua (set null để DAO giữ nguyên)
      boolean isAdmin = cu.getRole()!=null && cu.getRole().equalsIgnoreCase("ADMIN");
      Long departmentId = isAdmin ? toLong(req.getParameter("departmentId")) : null;
      Long divisionId   = isAdmin ? toLong(req.getParameter("divisionId"))   : null;
      Long managerId    = isAdmin ? toLong(req.getParameter("managerId"))    : null;
      Long roleIdToSave = isAdmin ? toLong(req.getParameter("roleId"))       : null;

      // validate
      if (notBlank(email) && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
        flashErr(req, "Email không hợp lệ"); resp.sendRedirect(req.getContextPath()+"/profile"); return;
      }
      if (notBlank(phone) && !phone.matches("^[0-9+()\\-\\s]{6,20}$")) {
        flashErr(req, "Số điện thoại không hợp lệ"); resp.sendRedirect(req.getContextPath()+"/profile"); return;
      }

      LocalDate birthday = null;
      if (notBlank(birthdayS)) {
        try { birthday = LocalDate.parse(birthdayS); }
        catch (Exception ex) { flashErr(req,"Định dạng ngày sinh phải là yyyy-MM-dd"); resp.sendRedirect(req.getContextPath()+"/profile"); return; }
      }

      boolean ok = userDAO.updateProfile(
          cu.getId(),
          nullIfBlank(fullName),
          nullIfBlank(email),
          nullIfBlank(phone),
          nullIfBlank(address),
          birthday,
          nullIfBlank(bio),
          nullIfBlank(avatarUrl),
          divisionId,
          departmentId,
          managerId,
          roleIdToSave
      );

      if (ok) {
        if (notBlank(fullName)) {
          cu.setFullName(fullName);
          req.getSession().setAttribute("currentUser", cu);
        }
        flashOk(req, "Cập nhật thành công!");
      } else {
        flashErr(req, "Cập nhật thất bại. Vui lòng thử lại.");
      }

      resp.sendRedirect(req.getContextPath()+"/profile"); // PRG
    } catch (Exception e) {
      log("ProfileServlet POST error", e);
      throw new ServletException("Không cập nhật được hồ sơ người dùng", e);
    }
  }

  // ===== helpers =====
  private static String trim(String s){ return s==null?null:s.trim(); }
  private static boolean notBlank(String s){ return s!=null && !s.isBlank(); }
  private static String nullIfBlank(String s){ return (s==null || s.isBlank()) ? null : s; }
  private static Long toLong(String s){
    try { return (s==null||s.isBlank())?null:Long.valueOf(s.trim()); }
    catch (Exception e){ return null; }
  }
  private static <T> List<T> safeList(List<T> l){ return (l==null)?Collections.emptyList():l; }
  private static void moveFlash(HttpServletRequest r, String fromKey, String toAttr){
    HttpSession s = r.getSession(false);
    if (s == null) return;
    Object v = s.getAttribute(fromKey);
    if (v != null){ r.setAttribute(toAttr, v); s.removeAttribute(fromKey); }
  }
  private static void flashOk(HttpServletRequest r, String msg){ r.getSession().setAttribute("flash_ok", msg); }
  private static void flashErr(HttpServletRequest r, String msg){ r.getSession().setAttribute("flash_err", msg); }
  private static String nz(String s){ return s==null?"":s.trim(); }
  private static String nonEmpty(String s){ return nz(s).isEmpty()? null : s; }
  private static String join(String sep, String... parts){
    List<String> ok = new ArrayList<>();
    for (String p: parts) if (p!=null && !p.isBlank()) ok.add(p);
    return String.join(sep, ok);
  }
}
