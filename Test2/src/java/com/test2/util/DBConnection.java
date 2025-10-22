package com.test2.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // TODO: sửa user/pass cho SQL Server của bạn
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=Test2;encrypt=true;trustServerCertificate=true";
    private static final String USER = "ducvps";
    private static final String PASS = "Mtdvpscom1@";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Missing SQLServer JDBC driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
