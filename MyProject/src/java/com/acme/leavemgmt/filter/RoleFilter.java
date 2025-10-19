/*
 * RoleFilter – chặn truy cập theo vai trò
 * Áp dụng cho: /request/approve và /admin/*
 */
package com.acme.leavemgmt.filter;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Objects;

@WebFilter(filterName = "RoleFilter", urlPatterns = {"/request/approve", "/admin/*"})
public class RoleFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        // Cho qua preflight nếu cần
        if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // Kiểm tra đăng nhập
        User me = (User) r.getSession().getAttribute("user");
        if (me == null) {
            // Đồng bộ với form login của bạn: /auth/login
            w.sendRedirect(r.getContextPath() + "/auth/login");
            return;
        }

        // Lấy role; mặc định EMPLOYEE nếu null
        String role = Objects.toString(me.getRoleCode(), "EMPLOYEE").toUpperCase();

        // Lấy đường dẫn tương đối (không gồm context)
        String uri         = r.getRequestURI();
        String contextPath = r.getContextPath();
        String path        = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;

        boolean allowed = isAllowed(path, role);

        if (!allowed) {
            // Forward tới trang 403 nếu có; nếu không, trả lỗi 403
            r.setAttribute("code", 403);
            try {
                r.getRequestDispatcher("/WEB-INF/views/errors/403.jsp").forward(r, w);
            } catch (Exception ignore) {
                w.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        // Pass xuống tầng dưới
        chain.doFilter(req, res);
    }

    /**
     * Quản trị (/admin/*): ADMIN | LEADER
     * Duyệt đơn (/request/approve[/*]): MANAGER | ADMIN
     */
    private boolean isAllowed(String path, String role) {
        // /admin/**
        if (path.startsWith("/admin/")) {
            return "ADMIN".equals(role) || "LEADER".equals(role);
        }
        // /request/approve hoặc /request/approve/...
        if (path.equals("/request/approve") || path.startsWith("/request/approve/")) {
            return "MANAGER".equals(role) || "ADMIN".equals(role);
        }
        // Mặc định (không match filter patterns) – cho qua
        return true;
    }
}
