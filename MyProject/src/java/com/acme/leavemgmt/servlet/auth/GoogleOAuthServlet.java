package com.acme.leavemgmt.servlet.auth;

import com.acme.leavemgmt.bootstrap.AppInit;
import com.acme.leavemgmt.dao.SysSettingsDAO;
import com.acme.leavemgmt.dao.UserDAO;
import com.acme.leavemgmt.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/oauth/google/start", "/oauth/google/callback"})
public class GoogleOAuthServlet extends HttpServlet {
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private DataSource ds;

    @Override
    public void init() {
        ds = AppInit.getDataSource();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String path = req.getServletPath();
        if (path.endsWith("/start")) {
            startOAuth(req, resp);
        } else if (path.endsWith("/callback")) {
            handleCallback(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    private void startOAuth(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        Map<String, String> s;
        try (SysSettingsDAO dao = new SysSettingsDAO(ds)) {
            s = dao.loadAll();
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        if (!"1".equals(s.getOrDefault("oauth_google_enabled", "0"))) {
            resp.sendError(404); return;
        }

        String clientId = s.get("google_client_id");
        String redirect = s.get("google_redirect");
        if (isBlank(clientId) || isBlank(redirect)) {
            resp.sendError(500, "Google OAuth chưa được cấu hình đúng."); return;
        }

        String state = UUID.randomUUID().toString();
        req.getSession(true).setAttribute("oauth_state", state);

        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirect)
                + "&scope=" + enc("openid email profile")
                + "&access_type=online"
                + "&prompt=select_account"
                + "&state=" + enc(state);

        resp.sendRedirect(authUrl);
    }

    private void handleCallback(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session == null) { backToLogin(req, resp, "Phiên đăng nhập hết hạn."); return; }

        String state = req.getParameter("state");
        String code  = req.getParameter("code");
        String saved = (String) session.getAttribute("oauth_state");
        session.removeAttribute("oauth_state"); // dọn state sau khi dùng

        if (isBlank(state) || !state.equals(saved)) { backToLogin(req, resp, "Phiên xác thực không hợp lệ."); return; }
        if (isBlank(code)) { backToLogin(req, resp, "Thiếu mã xác thực Google."); return; }

        Map<String, String> s;
        try (SysSettingsDAO dao = new SysSettingsDAO(ds)) {
            s = dao.loadAll();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        if (!"1".equals(s.getOrDefault("oauth_google_enabled", "0"))) {
            backToLogin(req, resp, "OAuth đang tắt."); return;
        }

        String clientId = s.getOrDefault("google_client_id", "");
        String secret   = s.getOrDefault("google_client_secret", "");
        String redirect = s.getOrDefault("google_redirect", "");

        // Đổi code -> token
        String body = "code=" + enc(code)
                + "&client_id=" + enc(clientId)
                + "&client_secret=" + enc(secret)
                + "&redirect_uri=" + enc(redirect)
                + "&grant_type=authorization_code";

        HttpRequest tokenReq = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> tokenResp;
        try { tokenResp = HTTP.send(tokenReq, HttpResponse.BodyHandlers.ofString()); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); backToLogin(req, resp, "Lỗi khi đổi token."); return; }

        if (tokenResp.statusCode() != 200) {
            backToLogin(req, resp, "Không lấy được token từ Google."); return;
        }

        String idToken = readJson(tokenResp.body(), "id_token");
        if (isBlank(idToken)) { backToLogin(req, resp, "Không có id_token."); return; }

        // Verify id_token tại Google
        HttpRequest infoReq = HttpRequest.newBuilder(
                URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + enc(idToken)))
                .GET().build();

        HttpResponse<String> infoResp;
        try { infoResp = HTTP.send(infoReq, HttpResponse.BodyHandlers.ofString()); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); backToLogin(req, resp, "Lỗi xác minh token."); return; }

        if (infoResp.statusCode() != 200) {
            backToLogin(req, resp, "Token không hợp lệ."); return;
        }

        String email    = readJson(infoResp.body(), "email");
        String verified = readJson(infoResp.body(), "email_verified");
        String aud      = readJson(infoResp.body(), "aud");

        if (!clientId.equals(aud)) { backToLogin(req, resp, "aud không khớp client."); return; }
        if (isBlank(email) || !"true".equalsIgnoreCase(verified)) { backToLogin(req, resp, "Email chưa xác minh."); return; }

        // Giới hạn domain nếu có
        String allowedDomain = s.getOrDefault("google_allowed_domain", "").trim();
        if (!allowedDomain.isEmpty()) {
            String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
            if (!domain.equalsIgnoreCase(allowedDomain)) {
                backToLogin(req, resp, "Email không thuộc domain cho phép."); return;
            }
        }

        // Chỉ cho user đã tồn tại
        try (UserDAO dao = new UserDAO(ds)) {
            User u = dao.findByEmail(email);
            if (u == null) { backToLogin(req, resp, "Email chưa được khai báo trong hệ thống."); return; }
            HttpSession sss = req.getSession(true);
            sss.setAttribute("currentUser", u);
            resp.sendRedirect(req.getContextPath() + "/portal");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /* ================= helpers ================= */

    private static void backToLogin(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login?error=" + enc(msg));
    }

    private static String readJson(String json, String key) {
        if (json == null) return null;
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (m.find()) return m.group(1);
        // thử kiểu không có ngoặc kép (true/false/số)
        m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([^,}\\s]+)").matcher(json);
        return m.find() ? m.group(1).replaceAll("[\"}]", "") : null;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
