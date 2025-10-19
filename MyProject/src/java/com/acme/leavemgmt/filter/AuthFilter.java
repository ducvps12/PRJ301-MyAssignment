package com.acme.leavemgmt.filter;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
// KHÔNG dùng annotation ở đây vì web.xml metadata-complete=true
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override public void init(FilterConfig filterConfig) {}
    @Override public void destroy() {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        // Cho preflight qua
        if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String context = r.getContextPath();                     // vd: /MyProject
        String uri     = r.getRequestURI();                      // vd: /MyProject/auth/login
        String path    = uri.startsWith(context) ? uri.substring(context.length()) : uri; // vd: /auth/login

        // Whitelist các route/public resource
        if (isPublic(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = r.getSession(false);
        boolean loggedIn = false;

        if (session != null) {
            Object u = session.getAttribute("user");
            if (u instanceof User) loggedIn = true;
            else loggedIn = (session.getAttribute("userId") != null);
        }

        if (loggedIn) {
            chain.doFilter(req, res);
            return;
        }

        // Nếu là AJAX → trả 401 thay vì redirect
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(r.getHeader("X-Requested-With"));
        if (isAjax) {
            w.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            w.setHeader("X-Auth-Required", "1");
            return;
        }

        // Tránh redirect vòng lặp khi đang ở đúng URL login (GET hoặc POST)
        String loginUrl1 = context + "/auth/login";
        String loginUrl2 = context + "/login";
        if (uri.equals(loginUrl1) || uri.equals(loginUrl2)) {
            chain.doFilter(req, res);
            return;
        }

        // Ghi nhớ URL cũ để login xong quay lại (optional)
        String q = r.getQueryString();
        String original = path + (q != null ? "?" + q : "");
        r.getSession(true).setAttribute("redirectAfterLogin", original);

        // Redirect sang /auth/login (chuẩn bạn đang dùng)
        w.sendRedirect(loginUrl1);
    }

    /** Những đường dẫn công khai không cần đăng nhập */
    private boolean isPublic(String path) {
        // Trang gốc / welcome
        if (path.equals("/") || path.equals("/index.jsp")) return true;

        // Trang login / logout / register / health
        if (path.startsWith("/auth/login") ||
            path.startsWith("/login") ||                  // ← THÊM để khớp web.xml
            path.startsWith("/auth/do-login") ||
            path.startsWith("/auth/logout") ||
            path.startsWith("/auth/register") ||
            path.startsWith("/__health")) {
            return true;
        }

        // Tài nguyên tĩnh
        if (path.startsWith("/assets/") ||
            path.startsWith("/static/") ||
            path.startsWith("/public/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/img/") ||
            path.startsWith("/images/") ||
            path.endsWith(".css") ||
            path.endsWith(".js") ||
            path.endsWith(".png") ||
            path.endsWith(".jpg") ||
            path.endsWith(".jpeg") ||
            path.endsWith(".gif") ||
            path.endsWith(".svg") ||
            path.endsWith(".ico") ||
            path.endsWith(".webp")) {
            return true;
        }

        // API public (nếu có)
        // if (path.startsWith("/api/public/")) return true;

        return false;
    }
}
