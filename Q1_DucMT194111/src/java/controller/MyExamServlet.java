// src/main/java/controller/MyExamServlet.java
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name="MyExamServlet", urlPatterns={"/my-exam"})
public class MyExamServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain; charset=UTF-8");
    req.setCharacterEncoding("UTF-8");

    String sa = req.getParameter("txta");
    String sb = req.getParameter("txtb");

    try (PrintWriter out = resp.getWriter()) {
      int a = Integer.parseInt(sa.trim());
      int b = Integer.parseInt(sb.trim());
      if (a > b) { resp.setStatus(400); out.print("You must input a<=b"); return; }
      long sum = 0;
      for (int i = a; i <= b; i++) if (isPrime(i)) sum += i;
      out.print(sum); // trả về chỉ kết quả
    } catch (NumberFormatException e) {
      resp.setStatus(400);
      resp.getWriter().print("Input must be integers");
    }
  }

  private static boolean isPrime(int n){
    if(n<2) return false;
    if(n%2==0) return n==2;
    for(int i=3;i*i<=n;i+=2) if(n%i==0) return false;
    return true;
  }
}
