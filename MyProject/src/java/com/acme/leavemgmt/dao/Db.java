package com.acme.leavemgmt.dao;

import com.acme.leavemgmt.util.DBConnection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Helper cấp thấp cho JDBC. */
public final class Db {

    static DataSource ds() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
  private Db(){}

  /** Ưu tiên DataSource (JNDI); nếu không có → dùng DBConnection util. */
  public static Connection getConnection() {
    DataSource ds = DataSourceProvider.get();
    try {
      if (ds != null) return ds.getConnection();
    } catch (Exception e) {
      throw new RuntimeException("Cannot borrow connection from DataSource", e);
    }
    try {
      return DBConnection.getConnection();
    } catch (Exception e) {
      throw new RuntimeException("Cannot obtain connection via DBConnection", e);
    }
  }

  public static void closeQuietly(AutoCloseable c) {
    if (c == null) return;
    try { c.close(); } catch (Exception ignore) {}
  }

  public static void rollbackQuietly(Connection c) {
    if (c == null) return;
    try { c.rollback(); } catch (Exception ignore) {}
  }

  public static void closeAll(ResultSet rs, Statement st, Connection cn) {
    closeQuietly(rs);
    closeQuietly(st);
    closeQuietly(cn);
  }

  /** Transaction helper: truyền lambda làm việc trong 1 transaction. */
  public interface TxWork<T> { T run(Connection c) throws Exception; }

  public static <T> T inTransaction(TxWork<T> work) {
    Connection c = null;
    try {
      c = getConnection();
      c.setAutoCommit(false);
      T val = work.run(c);
      c.commit();
      return val;
    } catch (Exception ex) {
      rollbackQuietly(c);
      throw (ex instanceof RuntimeException) ? (RuntimeException) ex : new RuntimeException(ex);
    } finally {
      if (c != null) try { c.setAutoCommit(true); } catch (Exception ignore) {}
      closeQuietly(c);
    }
  }
}
