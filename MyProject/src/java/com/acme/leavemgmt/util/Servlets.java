package com.acme.leavemgmt.util;

import com.acme.leavemgmt.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Servlets {
    private Servlets(){}

    // ===== Params helpers =====
    /** Parse Integer an toàn; null nếu rỗng/không phải số */
    public static Integer safeInt(String parameter) {
        if (parameter == null) return null;
        String s = parameter.trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Trim string, trả "" nếu null */
    public static String trim(String s){ return (s == null) ? "" : s.trim(); }

    /** Lấy param int với mặc định */
    public static int paramInt(HttpServletRequest req, String name, int def){
        try { return Integer.parseInt(req.getParameter(name)); }
        catch(Exception e){ return def; }
    }

    // ===== Session / Auth helpers =====
    /** Lấy ID user hiện tại từ session (ưu tiên object User nếu có) */
    public static int currentUserId(HttpServletRequest req){
        HttpSession ss = req.getSession(false);
        if (ss == null) return 0;

        Object cu = ss.getAttribute("currentUser");
        if (cu instanceof User u) {
            return u.getId();
        }

        // Thử các key phổ biến khác
        Object v = firstNonNull(
                ss.getAttribute("currentUserId"),
                ss.getAttribute("userId"),
                ss.getAttribute("uid")
        );
        if (v instanceof Integer i) return i;
        if (v != null) {
            try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException ignore) {}
        }
        return 0;
    }

    /** Lấy tên đầy đủ user hiện tại (fallback sang username nếu trống) */
    public static String currentUserFullname(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        if (ss == null) return "";

        Object cu = ss.getAttribute("currentUser");
        if (cu instanceof User u) {
            String name = nvl(u.getFullname(), u.getFullName()); // hỗ trợ cả fullname & fullName
            if (isBlank(name)) name = u.getUsername();
            return nvl(name, "");
        }

        // Fallback qua các key khác
        Object v = firstNonNull(
                ss.getAttribute("currentUserName"),
                ss.getAttribute("userFullname"),
                ss.getAttribute("fullName"),
                ss.getAttribute("fullname"),
                ss.getAttribute("username")
        );
        return (v == null) ? "" : String.valueOf(v);
    }

    /** Lấy role hiện tại (chuẩn hoá UPPER) */
    public static String currentRole(HttpServletRequest req){
        HttpSession ss = req.getSession(false);
        if (ss == null) return "";
        String role = null;

        Object cu = ss.getAttribute("currentUser");
        if (cu instanceof User u) role = u.getRole();
        if (isBlank(role)) {
            Object v = firstNonNull(
                    ss.getAttribute("currentRole"),
                    ss.getAttribute("role"),
                    ss.getAttribute("roles") // có nơi lưu CSV
            );
            role = (v == null) ? "" : String.valueOf(v);
        }
        return nvl(role, "").toUpperCase();
    }

    /** Có phải cấp quản lý? (hỗ trợ nhiều role phổ biến) */
    public static boolean isManager(HttpServletRequest req){
        String r = currentRole(req);
        // nếu là CSV roles -> kiểm tra contains
        String rolesCsv = "," + r + ",";
        return containsRole(rolesCsv, "ADMIN")
            || containsRole(rolesCsv, "SYS_ADMIN")
            || containsRole(rolesCsv, "DIV_LEADER")
            || containsRole(rolesCsv, "TEAM_LEAD")
            || containsRole(rolesCsv, "QA_LEAD")
            || containsRole(rolesCsv, "HR_ADMIN")
            || containsRole(rolesCsv, "MANAGER");
    }

    // ===== Small utils =====
    private static boolean containsRole(String csvUpper, String roleUpper){
        return csvUpper.contains("," + roleUpper + ",");
    }
    private static Object firstNonNull(Object... arr){
        for (Object o : arr) if (o != null) return o;
        return null;
    }
    private static boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
    private static String nvl(String s, String def){ return isBlank(s) ? def : s; }
    private static String nvl(String a, String b, String def){
        return !isBlank(a) ? a : (!isBlank(b) ? b : def);
    }
}
