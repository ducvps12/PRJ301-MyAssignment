<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html><html lang="vi"><head>
<meta charset="utf-8"><title>Create Course</title>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
body{font-family:system-ui,Segoe UI,Roboto,Arial;background:#f7f7f8;margin:0}
.wrap{max-width:720px;margin:40px auto;padding:0 16px}
.card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;box-shadow:0 2px 8px rgba(0,0,0,.04);padding:20px}
.row{display:flex;gap:12px;margin-bottom:12px;align-items:center}
label{min-width:110px;color:#374151}
input[type=text],input[type=date],select{width:100%;padding:10px;border:1px solid #e5e7eb;border-radius:8px}
.btn{padding:10px 16px;border-radius:10px;border:1px solid #e5e7eb;background:#111827;color:#fff;cursor:pointer}
.msg{color:#b91c1c;margin-bottom:12px}
</style></head><body>
<div class="wrap"><div class="card">
  <div class="msg">${msg}</div>

  <form method="post" action="${pageContext.request.contextPath}/create">
    <div class="row">
      <label for="name">Name</label>
      <input id="name" name="name" required>
    </div>
    <div class="row">
      <label for="from">From</label>
      <input id="from" type="date" name="from" required>
    </div>
    <div class="row">
      <label for="to">To</label>
      <input id="to" type="date" name="to" required>
    </div>
    <div class="row">
      <label for="online">Online</label>
      <input id="online" type="checkbox" name="online">
    </div>
    <div class="row">
      <label for="subject">Subject</label>
      <select id="subject" name="subject" required>
        <%
          try (java.sql.Connection cn = com.test2.util.DBConnection.getConnection();
               java.sql.PreparedStatement ps = cn.prepareStatement(
                   "SELECT subid, subname FROM dbo.Subject ORDER BY subname");
               java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
        %>
          <option value="<%= rs.getInt(1) %>"><%= rs.getString(2) %></option>
        <%
            }
          } catch (Exception e) { out.print("<option disabled>Lỗi load Subject</option>"); }
        %>
      </select>
    </div>
    <button class="btn" type="submit">Save</button>
  </form>
</div></div>
</body></html>
