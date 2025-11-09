// com.acme.leavemgmt.dao.PasswordResetDAO
package com.acme.leavemgmt.dao;


import java.sql.*;
import static com.acme.leavemgmt.util.DBConnection.getConnection;


public class PasswordResetDAO {
public void insert(int userId, String email, String otp, Timestamp expiresAt, String ip) throws SQLException {
final String sql = "INSERT INTO [dbo].[PasswordReset](user_id, email, otp_code, expires_at, created_ip) VALUES(?,?,?,?,?)";
try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
ps.setInt(1, userId);
ps.setString(2, email);
ps.setString(3, otp);
ps.setTimestamp(4, expiresAt);
ps.setString(5, ip);
ps.executeUpdate();
}
}


public ResetRow findActiveByEmail(String email) throws SQLException {
final String sql = "SELECT TOP 1 id, user_id, email, otp_code, expires_at, attempts, used " +
"FROM [dbo].[PasswordReset] WHERE email=? AND used=0 AND expires_at>SYSUTCDATETIME() " +
"ORDER BY id DESC";
try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
ps.setString(1, email);
try (ResultSet rs = ps.executeQuery()) {
if (rs.next()) {
ResetRow r = new ResetRow();
r.id = rs.getLong("id");
r.userId = rs.getInt("user_id");
r.email = rs.getString("email");
r.otp = rs.getString("otp_code");
r.expiresAt = rs.getTimestamp("expires_at");
r.attempts = rs.getInt("attempts");
r.used = rs.getBoolean("used");
return r;
}
}
}
return null;
}


public void incrementAttempts(long id) throws SQLException {
try (Connection c = getConnection(); Statement st = c.createStatement()) {
st.executeUpdate("UPDATE [dbo].[PasswordReset] SET attempts = attempts + 1 WHERE id=" + id);
}
}


public void markUsed(long id) throws SQLException {
try (Connection c = getConnection(); Statement st = c.createStatement()) {
st.executeUpdate("UPDATE [dbo].[PasswordReset] SET used = 1 WHERE id=" + id);
}
}


public int countRecentRequests(String email) throws SQLException {
final String sql = "SELECT COUNT(*) FROM [dbo].[PasswordReset] WHERE email=? AND created_at > DATEADD(minute, -15, SYSUTCDATETIME())";
try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
ps.setString(1, email);
try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
}
}


public static class ResetRow {
public long id; public int userId; public String email; public String otp; public Timestamp expiresAt; public int attempts; public boolean used;
}
}