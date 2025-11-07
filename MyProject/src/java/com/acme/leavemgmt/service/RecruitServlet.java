package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.RecruitDAO;

import javax.sql.DataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@WebServlet(urlPatterns = {"/recruit", "/recruit/job", "/recruit/app"})
public class RecruitServlet extends HttpServlet {

    private RecruitDAO dao;

    @Override public void init() {
        DataSource ds = (DataSource) getServletContext().getAttribute("DS");
        dao = new RecruitDAO(ds);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = n(req.getPathInfo());          // có thể null khi vào /recruit
        if ("/job".equals(path) || path == null) {   // Danh sách job
            String q = n(req.getParameter("q"));
            String status = n(req.getParameter("status"));
            int page = intParam(req,"page", 1);
            req.setAttribute("jobs", dao.jobs(q, status, page, 20));
            req.getRequestDispatcher("/WEB-INF/views/recruit/jobs.jsp").forward(req, resp);
            return;
        }

        if ("/app".equals(path)) {                   // Danh sách ứng viên theo job / stage
            Long jobId = asLong(req.getParameter("jobId"));
            String stage = n(req.getParameter("stage"));
            int page = intParam(req,"page", 1);
            req.setAttribute("apps", dao.applications(jobId, stage, page, 20));
            req.getRequestDispatcher("/WEB-INF/views/recruit/apps.jsp").forward(req, resp);
            return;
        }

        // fallback
        resp.sendRedirect(req.getContextPath()+"/recruit/job");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // nếu bạn dùng CSRF riêng thì gọi ở đây, ví dụ: Csrf.verify(req);

        String act = n(req.getParameter("act"));

        if ("moveStage".equals(act)) {
            Long appId   = asLong(req.getParameter("id"));
            String next  = n(req.getParameter("next"));
            int score    = intParam(req, "score", 0);
            String note  = n(req.getParameter("note"));

            Object me = req.getSession().getAttribute("currentUser");
            Long actorId = asLong(getId(me));  // chịu được User/Map/Number/String

            dao.moveStage(appId, next, score, note, actorId);
            resp.sendRedirect(req.getHeader("Referer"));
            return;
        }

        // (mở rộng các act khác ở đây…)
        resp.sendRedirect(req.getHeader("Referer"));
    }

    /* ================= Helpers ================= */

    private static String n(String s){ return s==null ? "" : s.trim(); }

    private static int intParam(HttpServletRequest r, String k, int d){
        try { return Integer.parseInt(n(r.getParameter(k))); } catch (Exception e){ return d; }
    }

    /** Lấy id từ User/Map/Number/String an toàn */
    private static Object getId(Object me){
        if (me == null) return null;
        try { // kiểu User có getId()
            return me.getClass().getMethod("getId").invoke(me);
        } catch (Exception ignore) {
            if (me instanceof Map<?,?> m) return m.get("id");
            return me;
        }
    }

    private static Long asLong(Object v){
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        return Long.valueOf(String.valueOf(v).trim());
    }
}
