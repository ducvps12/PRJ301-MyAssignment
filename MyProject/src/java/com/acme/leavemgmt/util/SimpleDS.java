package com.acme.leavemgmt.util;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/** DataSource rất gọn: lấy Connection trực tiếp từ DBConnection của bạn. */
public class SimpleDS implements DataSource {

  @Override
  public Connection getConnection() throws SQLException {
    return DBConnection.getConnection(); // <- dùng hàm bạn đã có
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    // Với cách dùng hiện tại, bỏ qua user/pass, vẫn dùng cấu hình trong DBConnection
    return DBConnection.getConnection();
  }

  // Các method không dùng tới:
  @Override public PrintWriter getLogWriter() { return null; }
  @Override public void setLogWriter(PrintWriter out) {}
  @Override public void setLoginTimeout(int seconds) {}
  @Override public int getLoginTimeout() { return 0; }
  @Override public Logger getParentLogger() { return Logger.getGlobal(); }
  @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
  @Override public boolean isWrapperFor(Class<?> iface) { return false; }
}
