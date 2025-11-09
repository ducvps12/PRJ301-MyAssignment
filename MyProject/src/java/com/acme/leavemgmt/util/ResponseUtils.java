package com.acme.leavemgmt.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ResponseUtils {

    public static void redirectWithMessage(HttpServletRequest req, HttpServletResponse resp, String string, String đặt_lại_mật_khẩu_thành_công_Vui_lòng_đăng) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
  private ResponseUtils(){}

  public static void json(HttpServletResponse resp, int code, String body) throws IOException {
    resp.setStatus(code);
    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write(body==null?"{}":body);
  }

  public static void text(HttpServletResponse resp, int code, String body) throws IOException {
    resp.setStatus(code);
    resp.setContentType("text/plain; charset=UTF-8");
    resp.getWriter().write(body==null?"":body);
  }
}
