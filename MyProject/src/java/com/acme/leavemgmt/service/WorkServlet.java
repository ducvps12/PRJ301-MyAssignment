package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.WorkDAO;
import com.acme.leavemgmt.model.WorkReport;

import javax.sql.DataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WorkServlet – trang Báo cáo công việc & Todo
 * URL:
 *   GET  /work           -> danh sách báo cáo (filter: q/type/from/to)
 *   GET  /work/todos     -> danh sách việc cần làm (status/assignee)
 *   POST /work           -> act=saveReport | addTodo | setTodoStatus
 */
@WebServlet(urlPatterns = {"/work", "/work/todos"})
public class WorkServlet extends HttpServlet {

    private WorkDAO dao;

    @Override
    public void init() {
        DataSource ds = (DataSource) getServletContext().getAttribute("DS");
        dao = new WorkDAO(ds);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = n(req.getPathInfo());
        int page = intParam(req, "page", 1);

        if ("/todos".equals(path)) {
            String status = n(req.getParameter("status"));               // OPEN/DOING/DONE/...
            Long assignee = asLong(req.getParameter("assignee"));
            var rows = dao.listTodos(status, assignee, page, 20);
            req.setAttribute("todos", rows);
            req.setAttribute("status", status);
            req.setAttribute("assignee", assignee);
            req.setAttribute("page", page);
            req.getRequestDispatcher("/WEB-INF/views/work/todos.jsp").forward(req, resp);
            return;
        }

        // --------- Reports ----------
        String type = n(req.getParameter("type"));                       // DAILY/WEEKLY/...
        LocalDate from = optDate(req, "from", LocalDate.now().minusDays(14));
        LocalDate to   = optDate(req, "to",   LocalDate.now());

        Object me = req.getSession().getAttribute("currentUser");
        Long userId = asLong(getId(me));

        var reports = dao.listReports(userId, from, to, type);
        req.setAttribute("reports", reports);
        req.setAttribute("type", type);
        req.setAttribute("from", from);
        req.setAttribute("to", to);
        req.setAttribute("page", page);
        req.getRequestDispatcher("/WEB-INF/views/work/reports.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Nếu có middleware CSRF riêng, gọi tại đây: Csrf.verify(req);

        String act = n(req.getParameter("act"));
        Object me = req.getSession().getAttribute("currentUser");
        Long userId = asLong(getId(me));

        switch (act) {
            case "saveReport": {
                // Form gợi ý: date, type, summary, blockers, planNext, hours(optional), tags(optional)
                LocalDate date = optDate(req, "date", LocalDate.now());
                String type = n(req.getParameter("type"));
                String summary = n(req.getParameter("summary"));
                String blockers = n(req.getParameter("blockers"));
                String planNext = n(req.getParameter("planNext"));
                String tags = n(req.getParameter("tags"));
                String hoursStr = n(req.getParameter("hours"));

                // Gộp nội dung đẹp để lưu 1 field content
                String content = """
                        ## Summary
                        %s

                        ## Blockers
                        %s

                        ## Plan next
                        %s
                        """.formatted(emptyDash(summary), emptyDash(blockers), emptyDash(planNext)).trim();

                var wr = new WorkReport();
                wr.setUserId(userId);
                wr.setWorkDate(date);
                wr.setType(type);
                wr.setContent(content);
                wr.setTags(tags.isBlank() ? null : tags);
                if (!hoursStr.isBlank()) {
                    try { wr.setHours(new java.math.BigDecimal(hoursStr)); } catch (Exception ignore) { /* optional */ }
                }

                dao.upsertReport(wr);
                resp.sendRedirect(req.getContextPath()+"/work?type="+enc(type)+"&from="+date+"&to="+date);
                return;
            }

            case "addTodo": {
                String title = n(req.getParameter("title"));
                Long assignee = asLong(req.getParameter("assignee"));
                LocalDate due = optDate(req, "due", null);
                String priority = n(req.getParameter("priority"));      // LOW/NORMAL/HIGH
                String tags = n(req.getParameter("tags"));
                String note = n(req.getParameter("note"));
                dao.addTodo(title, assignee, due, priority, tags, note);
                resp.sendRedirect(req.getContextPath()+"/work/todos");
                return;
            }

            case "setTodoStatus": {
                long id = longParam(req, "id");
                String status = n(req.getParameter("status"));
                dao.setTodoStatus(id, status);
                resp.sendRedirect(req.getHeader("Referer"));
                return;
            }
        }

        // fallback
        resp.sendRedirect(req.getHeader("Referer"));
    }

    /* ===================== Helpers ===================== */

    private static String n(String s){ return s==null ? "" : s.trim(); }

    private static String enc(String s){
        try { return java.net.URLEncoder.encode(n(s), java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return n(s); }
    }

    private static int intParam(HttpServletRequest r, String k, int d){
        try { return Integer.parseInt(n(r.getParameter(k))); } catch (Exception e){ return d; }
    }

    private static long longParam(HttpServletRequest r, String k){
        return Long.parseLong(n(r.getParameter(k)));
    }

    private static LocalDate optDate(HttpServletRequest r, String k, LocalDate def){
        String v = n(r.getParameter(k));
        if (v.isBlank()) return def;
        try { return LocalDate.parse(v); } catch (Exception e){ return def; }
    }

    /** lấy id linh hoạt từ User/Map/Number/String */
    private static Object getId(Object me){
        if (me == null) return null;
        try { return me.getClass().getMethod("getId").invoke(me); }
        catch (Exception ignore) {
            if (me instanceof Map<?,?> m) return m.get("id");
            return me;
        }
    }

    private static Long asLong(Object v){
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : Long.valueOf(s);
    }

    private static String emptyDash(String s){ return s==null || s.isBlank() ? "—" : s; }
}
