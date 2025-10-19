package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Servlets {
    private Servlets(){}

    public static int currentUserId(HttpServletRequest req){
        Object v = req.getSession().getAttribute("userId");
        return (v instanceof Integer) ? (Integer)v : Integer.parseInt(String.valueOf(v));
    }
    public static String currentRole(HttpServletRequest req){
        Object v = req.getSession().getAttribute("role");
        return v == null ? "" : v.toString();
    }
    public static boolean isManager(HttpServletRequest req){
        return "MANAGER".equalsIgnoreCase(currentRole(req));
    }
    public static int paramInt(HttpServletRequest req, String name, int def){
        try { return Integer.parseInt(req.getParameter(name)); } catch(Exception e){ return def; }
    }
    public static String trim(String s){ return s == null ? "" : s.trim(); }
}
