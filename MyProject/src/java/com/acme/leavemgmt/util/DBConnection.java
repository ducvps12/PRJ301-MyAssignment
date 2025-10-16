package com.acme.leavemgmt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class DBConnection {

    // --- Sửa phù hợp máy bạn ---
    private static final String HOST = "localhost"; // hoặc IP
    private static final int    PORT = 1433;
    private static final String DB   = "P42_AssignmentW10"; // <-- sửa đúng tên DB
    // private static final String INSTANCE = ";instanceName=SQLEXPRESS";

    private static final String USER = "ducvps";
    private static final String PASS = "Mtdvpscom1@";

    private static String buildUrl() {
        return "jdbc:sqlserver://" + HOST + ":" + PORT
             + ";databaseName=" + DB
             // + INSTANCE
             + ";encrypt=true"
             + ";trustServerCertificate=true"
             + ";loginTimeout=5";
    }

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Seed admin (nếu bảng đã tồn tại)
            seedAdminIfMissing();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLServerDriver not found", e);
        } catch (Exception se) {
            System.err.println("[DBConnection] Seed admin failed: " + se.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(buildUrl(), USER, PASS);
    }
    
public static boolean ping() {
    try (Connection cn = getConnection();
         PreparedStatement ps = cn.prepareStatement("SELECT 1");
         java.sql.ResultSet rs = ps.executeQuery()) {
        return rs.next();
    } catch (SQLException e) {
        System.err.println("[DBConnection] Ping FAIL: " + e.getMessage()
                + " | SQLState=" + e.getSQLState()
                + " | Code=" + e.getErrorCode());
        return false;
    }
}


    /** Tạo admin nếu chưa có (schema legacy: [Users] có password/role/department) */
    private static void seedAdminIfMissing() throws SQLException {
        // Nếu chưa tạo bảng [Users] thì câu IF NOT EXISTS sẽ lỗi -> bạn có thể chạy DDL trước.
        String upsert = """
            IF NOT EXISTS (SELECT 1 FROM [Users] WHERE username = ?)
            BEGIN
              INSERT INTO [Users](username, password, full_name, role, department)
              VALUES(?, ?, N'System Admin', N'ADMIN', N'IT')
            END
            """;
        try (Connection cn = DriverManager.getConnection(buildUrl(), USER, PASS);
             PreparedStatement ps = cn.prepareStatement(upsert)) {

            // ?1: username (SELECT)
            ps.setString(1, "admin");
            // ?2: username (INSERT)
            ps.setString(2, "admin");
            // ?3: password (INSERT)
            ps.setString(3, "admin123"); // mật khẩu mặc định
            ps.executeUpdate();
        }
    }
}
