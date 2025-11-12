package com.acme.leavemgmt.util;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SimpleDataSource implements DataSource {
  @Override public Connection getConnection() throws SQLException {
    return DBConnection.getConnection();
  }
  @Override public Connection getConnection(String u, String p) throws SQLException {
    return DBConnection.getConnection(); // bỏ qua u/p vì đã cấu hình sẵn trong DBConnection
  }

  // Các method còn lại không cần dùng – trả mặc định
  @Override public PrintWriter getLogWriter() { return null; }
  @Override public void setLogWriter(PrintWriter out) {}
  @Override public void setLoginTimeout(int seconds) {}
  @Override public int getLoginTimeout() { return 0; }
  @Override public Logger getParentLogger() { return Logger.getGlobal(); }
  @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
  @Override public boolean isWrapperFor(Class<?> iface) { return false; }
}
