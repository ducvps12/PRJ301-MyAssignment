package com.acme.leavemgmt.servlet.account;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/portal")
public class PortalHomeServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // KPI mẫu; thay bằng DAO thật của bạn
    Map<String,Object> kpi = new LinkedHashMap<>();
    kpi.put("AL", 8);     // phép còn
    kpi.put("pending", 2);
    kpi.put("late", 1);
    kpi.put("net", 15_000_000);
    req.setAttribute("kpi", kpi);

    // Hoạt động gần đây (ví dụ)
    List<Map<String,Object>> acts = new ArrayList<>();
    acts.add(Map.of("type","REQUEST","title","Đơn nghỉ #1029 - PENDING","time", new Date()));
    req.setAttribute("recentActivities", acts);

    req.setAttribute("todaySummary", "08:32 → — (muộn 2')");

    req.getRequestDispatcher("/WEB-INF/views/portal/home.jsp").forward(req, resp);
  }
}
