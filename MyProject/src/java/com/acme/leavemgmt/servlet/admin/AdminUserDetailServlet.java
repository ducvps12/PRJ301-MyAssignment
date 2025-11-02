package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.AuditLog;
import com.acme.leavemgmt.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

@WebServlet(name = "AdminUserDetailServlet", urlPatterns = {"/admin/users/detail"})
public class AdminUserDetailServlet extends HttpServlet {

    /** Những role được phép xem chi tiết user (giống servlet list/create) */
    private static final String[] ALLOWED_ROLES = {
            "ADMIN",
            "HR",
            "DIV_LEADER"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String ctx = req.getContextPath();

        /* 1. Check đăng nhập */
        HttpSession ses = req.getSession(false);
        if (ses == null || ses.getAttribute("currentUser") == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }
        User me = (User) ses.getAttribute("currentUser");

        /* 2. Check quyền */
        if (!hasOneOfRoles(me, ALLOWED_ROLES)) {
            // không đủ quyền -> về dashboard
            resp.sendRedirect(ctx + "/dashboard");
            return;
        }

        /* 3. Lấy id */
        Integer id = parseInt(req.getParameter("id"));
        if (id == null || id <= 0) {
            // thiếu id thì quay lại list, để nó khỏi ném exception rồi redirect lung tung
            resp.sendRedirect(ctx + "/admin/users?err=missing_id");
            return;
        }

        /* 4. Query DB */
        // mở rộng SELECT để lấy luôn tên dept, tên role... từ DB của bạn (xem 2th1.sql)
        String sql =
                "SELECT u.id, u.username, u.full_name, u.email, u.role, u.department, u.status, " +
                "       d.name AS dept_name, r.name AS role_name, " +
                "       es.status_name AS emp_status_name, jt.title_name AS job_title_name " +
                "FROM Users u " +
                "LEFT JOIN Departments d ON u.department = d.code " +
                "LEFT JOIN Roles r ON u.role = r.code " +
                "LEFT JOIN Employment_Statuses es ON u.employment_status_code = es.status_code " +
                "LEFT JOIN Job_Titles jt ON u.job_title_id = jt.id " +
                "WHERE u.id = ?";

        User target = null;
        String deptName = null;
        String roleName = null;
        String empStatusName = null;
        String jobTitleName = null;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // không có user này -> quay lại list
                    resp.sendRedirect(ctx + "/admin/users?err=not_found");
                    return;
                }

                target = new User();
                target.setId(rs.getInt("id"));
                target.setUsername(rs.getString("username"));
                target.setFullName(rs.getString("full_name"));
                target.setEmail(rs.getString("email"));
                target.setRole(rs.getString("role"));             // ADMIN / STAFF / ...
                target.setDepartment(rs.getString("department")); // IT / QA / SALE ...

                // status trong DB có thể là 1/0 hoặc ACTIVE/INACTIVE
                String rawStatus = rs.getString("status");
                target.setStatus(normalizeStatus(rawStatus));

                deptName = rs.getString("dept_name");
                roleName = rs.getString("role_name");
                empStatusName = rs.getString("emp_status_name");
                jobTitleName = rs.getString("job_title_name");
            }

        } catch (SQLException e) {
            // nếu bạn đang cấu hình error-page -> nó sẽ về dashboard
            // nên ta ném ra vẫn OK, nhưng để dễ debug bạn có thể log lại ở đây
            throw new ServletException(e);
        }

        /* 5. Ghi audit (cho đẹp log giống mấy cái khác) */
        try {
            AuditLog.log(
                    me.getId(),
                    "ADMIN_USER_DETAIL_VIEW",
                    "view user id=" + id,
                    req.getRemoteAddr(),
                    req.getHeader("User-Agent")
            );
        } catch (Exception ignore) {
            // không để việc audit làm hỏng luồng chính
        }

        /* 6. Bơm data cho JSP */
        req.setAttribute("u", target);
        req.setAttribute("deptName", deptName);
        req.setAttribute("roleName", roleName);
        req.setAttribute("empStatusName", empStatusName);
        req.setAttribute("jobTitleName", jobTitleName);

        /* 7. Forward sang JSP hiển thị */
        // chú ý path phải đúng với JSP bạn đã tạo
        req.getRequestDispatcher("/WEB-INF/views/admin/user_detail.jsp")
                .forward(req, resp);
    }

    private boolean hasOneOfRoles(User me, String[] allowed) {
        if (me == null || me.getRole() == null) return false;
        String myRole = me.getRole();
        for (String r : allowed) {
            if (Objects.equals(r, myRole)) return true;
        }
        return false;
    }

    private Integer parseInt(String s) {
        try {
            return (s == null) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Chuyển mọi kiểu status trong DB về int cho hợp với model
     * DB của bạn có thể để 1/0 hoặc ACTIVE/INACTIVE
     */
    private int normalizeStatus(String raw) {
        if (raw == null) return 0;
        raw = raw.trim();
        if ("ACTIVE".equalsIgnoreCase(raw)) return 1;
        if ("INACTIVE".equalsIgnoreCase(raw)) return 0;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
