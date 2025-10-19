package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

public final class Csrf {
    private Csrf(){}

    public static void ensureToken(HttpSession s){
        if (s.getAttribute("_csrf") == null) {
            s.setAttribute("_csrf", UUID.randomUUID().toString());
        }
    }

    public static boolean valid(HttpServletRequest req){
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Object token = s.getAttribute("_csrf");
        String form = req.getParameter("_csrf");
        return token != null && token.equals(form);
    }
}
