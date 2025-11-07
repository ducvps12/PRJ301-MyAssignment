package com.acme.leavemgmt.service;

import com.acme.leavemgmt.dao.RecruitDAO;
import javax.sql.DataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
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

        // Phân nhánh theo servletPath (chính xác cho các mapping cố định)
        String sp = req.getServletPath(); // vd: "/recruit", "/recruit/job", "/recruit/app"

        switch (sp) {
            case "/recruit":
            case "/recruit/job": {
                String q = n(req.getParameter("q"));
                String status = n(req.getParameter("status"));
                int page = intParam(req, "page", 1);
                req.setAttribute("jobs", dao.jobs(q, status, page, 20));
                req.getRequestDispatcher("/WEB-INF/views/recruit/jobs.jsp").forward(req, resp);
                return;
            }
            case "/recruit/app": {
                Long jobId = asLong(req.getParameter("jobId"));
                String stage = n(req.getParameter("stage"));
                int page = intParam(req, "page", 1);
                req.setAttribute("apps", dao.applications(jobId, stage, page, 20));
                req.getRequestDispatcher("/WEB-INF/views/recruit/apps.jsp").forward(req, resp);
                return;
            }
            default:
                // Không nên redirect lại /recruit/job khi không rõ — có thể 404
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String act = n(req.getParameter("act"));

        if ("moveStage".equals(act)) {
            Long appId = asLong(req.getParameter("id"));
            String next = n(req.getParameter("next"));
            int score   = intParam(req, "score", 0);
            String note = n(req.getParameter("note"));

            Object me = req.getSession().getAttribute("currentUser");
            Long actorId = asLong(getId(me));

            dao.moveStage(appId, next, score, note, actorId);
            resp.sendRedirect(req.getHeader("Referer"));
            return;
        }
        resp.sendRedirect(req.getHeader("Referer"));
    }

    /* ================= Helpers ================= */
    private static String n(String s){ return s == null ? "" : s.trim(); }
    private static int intParam(HttpServletRequest r, String k, int d){
        try { return Integer.parseInt(n(r.getParameter(k))); } catch (Exception e){ return d; }
    }
    private static Object getId(Object me){
        if (me == null) return null;
        try { return me.getClass().getMethod("getId").invoke(me); }
        catch (Exception ignore) { if (me instanceof Map<?,?> m) return m.get("id"); return me; }
    }
    private static Long asLong(Object v){
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        return Long.valueOf(String.valueOf(v).trim());
    }
}
