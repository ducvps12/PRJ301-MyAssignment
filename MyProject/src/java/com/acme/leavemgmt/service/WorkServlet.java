package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.WorkDAO;
import com.acme.leavemgmt.model.WorkReport;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/work/*"})
public class WorkServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Resource(name = "jdbc/LeaveDB")
    private DataSource injectedDs;

    private WorkDAO dao;

    @Override
    public void init() {
        DataSource ds = injectedDs;
        if (ds == null) {
            Object ctxObj = getServletContext().getAttribute("DS");
            if (ctxObj instanceof DataSource) ds = (DataSource) ctxObj;
        }
        if (ds == null) {
            try {
                ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/LeaveDB");
                getServletContext().setAttribute("DS", ds);
            } catch (Exception e) {
                throw new IllegalStateException(
                    "DataSource jdbc/LeaveDB not found. Check context.xml + driver in tomcat/lib.", e);
            }
        }
        dao = new WorkDAO(ds);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // Chuẩn hoá path: null -> "", bỏ các dấu '/' dư ở cuối
        String rawPath = req.getPathInfo();                 // null | "/todos" | "/todos/"
        String path = (rawPath == null) ? "" : rawPath.replaceAll("/+$", "");
        int page = intParam(req, "page", 1);

        switch (path) {
            // ------------------- /work/todos -------------------
            case "/todos": {
                String status = nz(req.getParameter("status"));
                Long assignee = toLong(req.getParameter("assignee"));
                List<?> rows = dao.listTodos(status, assignee, page, 20);

                req.setAttribute("todos", rows);
                req.setAttribute("status", status);
                req.setAttribute("assignee", assignee);
                req.setAttribute("page", page);
                req.getRequestDispatcher("/WEB-INF/views/work/todos.jsp").forward(req, resp);
                return;
            }

            // ------------------- /work (reports) -------------------
            case "":
            default: {
                String type = nz(req.getParameter("type")); // DAILY/WEEKLY/MONTHLY/...
                LocalDate from = dateParam(req, "from", LocalDate.now().minusDays(14));
                LocalDate to   = dateParam(req, "to",   LocalDate.now());

                Long userId = currentUserId(req);
                List<?> reports = dao.listReports(userId, from, to, type);

                req.setAttribute("reports", reports);
                req.setAttribute("type", type);
                req.setAttribute("from", from);
                req.setAttribute("to", to);
                req.setAttribute("page", page);
                req.getRequestDispatcher("/WEB-INF/views/work/reports.jsp").forward(req, resp);
                return;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String act = nz(req.getParameter("act"));
        Long userId = currentUserId(req);

        if ("saveReport".equals(act)) {
            LocalDate date   = dateParam(req, "date", LocalDate.now());
            String type      = nz(req.getParameter("type"));
            String summary   = nz(req.getParameter("summary"));
            String blockers  = nz(req.getParameter("blockers"));
            String planNext  = nz(req.getParameter("planNext"));
            String tags      = nz(req.getParameter("tags"));
            String hoursStr  = nz(req.getParameter("hours"));

            String content = ("## Summary\n"   + dashIfEmpty(summary)  + "\n\n" +
                              "## Blockers\n"  + dashIfEmpty(blockers) + "\n\n" +
                              "## Plan next\n" + dashIfEmpty(planNext)).trim();

            WorkReport wr = new WorkReport();
            wr.setUserId(userId);
            wr.setWorkDate(date);
            wr.setType(type);
            wr.setContent(content);
            wr.setTags(tags.isEmpty() ? null : tags);
            if (!hoursStr.isEmpty()) {
                try { wr.setHours(new BigDecimal(hoursStr)); } catch (Exception ignored) {}
            }

            dao.upsertReport(wr);
            resp.sendRedirect(req.getContextPath() + "/work?type=" + url(type) + "&from=" + date + "&to=" + date);
            return;
        }

        if ("addTodo".equals(act)) {
            String title    = nz(req.getParameter("title"));
            Long assignee   = toLong(req.getParameter("assignee"));
            LocalDate due   = dateParam(req, "due", null);
            String priority = nz(req.getParameter("priority"));
            String tags     = nz(req.getParameter("tags"));
            String note     = nz(req.getParameter("note"));

            dao.addTodo(title, assignee, due, priority, tags, note);
            resp.sendRedirect(req.getContextPath() + "/work/todos");
            return;
        }

        if ("setTodoStatus".equals(act)) {
            long id = longParam(req, "id", -1);
            String status = nz(req.getParameter("status"));
            if (id > 0 && !status.isEmpty()) {
                dao.setTodoStatus(id, status);
            }
            resp.sendRedirect(backOr(req, req.getContextPath() + "/work/todos"));
            return;
        }

        // fallback
        resp.sendRedirect(backOr(req, req.getContextPath() + "/work"));
    }

    // ===== Helpers =====

    private static String nz(String s){ return (s == null) ? "" : s.trim(); }

    private static String url(String s){
        try { return java.net.URLEncoder.encode(nz(s), StandardCharsets.UTF_8.name()); }
        catch (Exception e) { return nz(s); }
    }

    private static String backOr(HttpServletRequest r, String def){
        String ref = nz(r.getHeader("Referer"));
        return ref.isEmpty() ? def : ref;
    }

    private static int intParam(HttpServletRequest r, String k, int d){
        try { return Integer.parseInt(nz(r.getParameter(k))); } catch (Exception e){ return d; }
    }

    private static long longParam(HttpServletRequest r, String k, long d){
        try { return Long.parseLong(nz(r.getParameter(k))); } catch (Exception e){ return d; }
    }

    private static LocalDate dateParam(HttpServletRequest r, String k, LocalDate def){
        String v = nz(r.getParameter(k));
        if (v.isEmpty()) return def;
        try { return LocalDate.parse(v); } catch (Exception e){ return def; }
    }

    /** Lấy id linh hoạt từ session currentUser (User/Map/Number/String). */
    private static Long currentUserId(HttpServletRequest req){
        Object me = req.getSession().getAttribute("currentUser");
        if (me == null) return null;

        try { // getId() nếu có
            Object val = me.getClass().getMethod("getId").invoke(me);
            return toLong(val);
        } catch (Exception ignore) {}

        if (me instanceof Map) {
            Object idVal = ((Map<?, ?>) me).get("id");
            return toLong(idVal);
        }
        return toLong(me);
    }

    private static Long toLong(Object v){
        if (v == null) return null;
        if (v instanceof Long)    return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof Number)  return ((Number) v).longValue();
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : Long.valueOf(s);
    }

    private static String dashIfEmpty(String s){ return (s == null || s.trim().isEmpty()) ? "—" : s; }
}
