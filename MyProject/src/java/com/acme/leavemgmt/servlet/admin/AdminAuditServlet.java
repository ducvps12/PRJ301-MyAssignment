package com.acme.leavemgmt.servlet.admin;

import com.acme.leavemgmt.dao.ActivityDAO;
import com.acme.leavemgmt.dao.ActivityDAO.Page;
import com.acme.leavemgmt.model.Activity;
import com.acme.leavemgmt.model.User;
import com.acme.leavemgmt.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@WebServlet("/admin/audit") // LƯU Ý: nếu web.xml có metadata-complete="true" thì cần mapping trong web.xml
public class AdminAuditServlet extends HttpServlet {
    private final ActivityDAO dao = new ActivityDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Charset cho toàn request/response
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            // --- Kiểm tra quyền ADMIN ---
            User me = (User) req.getSession().getAttribute("currentUser");
            if (me == null || !"ADMIN".equalsIgnoreCase(me.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // --- Lấy & chuẩn hoá tham số ---
            Integer userId = paramInt(req, "userId");
            String action  = limit(trim(req.getParameter("action")), 100);
            String q       = limit(trim(req.getParameter("q")), 200);

            LocalDate from = paramDate(req, "from");
            LocalDate to   = paramDate(req, "to");
            // Nếu nhập ngược thì hoán đổi
            if (from != null && to != null && from.isAfter(to)) {
                LocalDate tmp = from; from = to; to = tmp;
            }

            int page = Math.max(1, paramInt(req, "page", 1));
            int size = Math.min(100, Math.max(10, paramInt(req, "size", 20)));

            // --- Export CSV ---
            if ("csv".equalsIgnoreCase(req.getParameter("export"))) {
                resp.setContentType("text/csv; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=audit-log.csv");

                // Thêm BOM để Excel nhận UTF-8 đúng tiếng Việt
                try (PrintWriter out = resp.getWriter()) {
                    out.write('\uFEFF');
                    dao.exportCsv(userId, action, q, from, to, out);
                } catch (Exception e) {
                    // Nếu lỗi khi xuất, trả 500 có thông điệp
                    resp.reset();
                    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
                return;
            }

            // --- Truy vấn & forward JSP ---
            Page<Activity> result = dao.search(userId, action, q, from, to, page, size);
            req.setAttribute("result", result);
            req.setAttribute("userId", userId);
            req.setAttribute("action", action);
            req.setAttribute("q", q);
            req.setAttribute("from", from);
            req.setAttribute("to", to);
            req.setAttribute("size", size);
            Csrf.addToken(req);

            req.getRequestDispatcher("/WEB-INF/views/admin/audit.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    // ===== Helpers =====
    private static String trim(String s) {
        return (s == null) ? null : s.trim();
    }

    private static String limit(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static Integer paramInt(HttpServletRequest req, String name) {
        try {
            String v = req.getParameter(name);
            return (v == null || v.isBlank()) ? null : Integer.parseInt(v);
        } catch (Exception e) {
            return null;
        }
    }

    private static int paramInt(HttpServletRequest req, String name, int def) {
        Integer v = paramInt(req, name);
        return v == null ? def : v;
    }

    private static LocalDate paramDate(HttpServletRequest req, String name) {
        try {
            String v = req.getParameter(name);
            return (v == null || v.isBlank()) ? null : LocalDate.parse(v);
        } catch (Exception e) {
            return null;
        }
    }
}
