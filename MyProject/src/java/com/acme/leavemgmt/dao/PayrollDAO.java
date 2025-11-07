package com.acme.leavemgmt.dao;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

/**
 * PayrollDAO – quản lý kỳ lương và các item lương.
 *
 * Hỗ trợ:
 *  - createRun(y, m): tạo kỳ lương nếu chưa có, trả về id
 *  - lockRun(runId): khóa kỳ
 *  - listItems(runId): danh sách item
 *  - upsertItem(pi): cập nhật/chen item theo (run_id, user_id)
 *  - findRun(y, m): tìm id kỳ lương
 *
 * Lưu ý: allowance/insurance/tax không có cột riêng trong bảng mẫu;
 * chúng được dùng để tính GROSS/NET khi upsert.
 */
public class PayrollDAO {

    private final DataSource ds;
    public PayrollDAO(DataSource ds) { this.ds = ds; }

    /* ======================= Model nội bộ ======================= */
    public static class PayrollItem {
        public Long id;
        public Long runId;
        public Long userId;

        // tiền tệ dùng BigDecimal
        public BigDecimal baseSalary = BigDecimal.ZERO;
        public BigDecimal allowance  = BigDecimal.ZERO;
        public BigDecimal otPay      = BigDecimal.ZERO; // map -> ot_amount
        public BigDecimal bonus      = BigDecimal.ZERO;
        public BigDecimal penalty    = BigDecimal.ZERO; // map -> fine
        public BigDecimal leaveDeduct= BigDecimal.ZERO;

        // không có cột riêng, dùng để tính net
        public BigDecimal insurance  = BigDecimal.ZERO;
        public BigDecimal tax        = BigDecimal.ZERO;

        // các giá trị lưu DB
        public BigDecimal gross      = BigDecimal.ZERO;
        public BigDecimal net        = BigDecimal.ZERO;

        public String note;
        public LocalDateTime updatedAt;

        // ===== setters dùng bởi servlet =====
        public void setRunId(Long runId)            { this.runId = runId; }
        public void setRunId(long runId)            { this.runId = runId; }
        public void setUserId(Long userId)          { this.userId = userId; }

        public void setBaseSalary(BigDecimal v)     { this.baseSalary = nz(v); }
        public void setAllowance(BigDecimal v)      { this.allowance  = nz(v); }
        public void setOtPay(BigDecimal v)          { this.otPay      = nz(v); }
        public void setBonus(BigDecimal v)          { this.bonus      = nz(v); }
        public void setPenalty(BigDecimal v)        { this.penalty    = nz(v); }
        public void setLeaveDeduct(BigDecimal v)    { this.leaveDeduct= nz(v); }

        public void setInsurance(BigDecimal v)      { this.insurance  = nz(v); }
        public void setTax(BigDecimal v)            { this.tax        = nz(v); }
        public void setNetPay(BigDecimal v)         { this.net        = nz(v); }
        public void setNote(String note)            { this.note = note; }
    }

    /* ======================= Utils ======================= */
    private static BigDecimal nz(BigDecimal d){ return d == null ? BigDecimal.ZERO : d; }
    private static BigDecimal bz(Object o){
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        try { return new BigDecimal(String.valueOf(o)); } catch (Exception e){ return BigDecimal.ZERO; }
    }

    private PayrollItem mapItem(ResultSet rs) throws SQLException {
        PayrollItem p = new PayrollItem();
        p.id          = rs.getLong("id");
        p.runId       = rs.getLong("run_id");
        p.userId      = rs.getLong("user_id");
        p.baseSalary  = rs.getBigDecimal("base_salary");  if (p.baseSalary  == null) p.baseSalary  = BigDecimal.ZERO;
        p.otPay       = rs.getBigDecimal("ot_amount");    if (p.otPay       == null) p.otPay       = BigDecimal.ZERO;
        p.bonus       = rs.getBigDecimal("bonus");        if (p.bonus       == null) p.bonus       = BigDecimal.ZERO;
        p.penalty     = rs.getBigDecimal("fine");         if (p.penalty     == null) p.penalty     = BigDecimal.ZERO;
        p.leaveDeduct = rs.getBigDecimal("leave_deduct"); if (p.leaveDeduct == null) p.leaveDeduct = BigDecimal.ZERO;
        p.gross       = rs.getBigDecimal("gross");        if (p.gross       == null) p.gross       = BigDecimal.ZERO;
        p.net         = rs.getBigDecimal("net");          if (p.net         == null) p.net         = BigDecimal.ZERO;
        p.note        = rs.getString("note");
        Timestamp uat = rs.getTimestamp("updated_at");
        p.updatedAt   = (uat==null?null:uat.toLocalDateTime());

        // allowance/insurance/tax không lưu DB -> để 0 khi đọc
        p.allowance = BigDecimal.ZERO;
        p.insurance = BigDecimal.ZERO;
        p.tax       = BigDecimal.ZERO;
        return p;
    }

    /* ======================= API ======================= */

    /** Tạo kỳ lương (nếu chưa có). Trả về id của kỳ. */
    public long createRun(int y, int m) {
        Optional<Long> existed = findRun(y, m);
        if (existed.isPresent()) return existed.get();

        final String SQL = "INSERT INTO payroll_runs([year],[month],locked,created_at) VALUES(?,?,0,?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, y);
            ps.setInt(2, m);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return findRun(y, m).orElseThrow(() -> new RuntimeException("Cannot obtain payroll run id"));
        } catch (SQLException e) {
            Optional<Long> fid = findRun(y, m);
            if (fid.isPresent()) return fid.get();
            throw new RuntimeException("PayrollDAO.createRun error", e);
        }
    }

    /** Khóa kỳ lương. */
    public boolean lockRun(Long runId) {
        final String SQL = "UPDATE payroll_runs SET locked=1, locked_at=? WHERE id=? AND locked=0";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, runId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.lockRun error", e);
        }
    }

    /** Danh sách item của một kỳ. */
    public List<PayrollItem> listItems(Long runId) {
        final String SQL =
            "SELECT id,run_id,user_id,base_salary,ot_amount,bonus,fine,leave_deduct,gross,net,note,updated_at " +
            "FROM payroll_items WHERE run_id=? ORDER BY user_id ASC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setLong(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                List<PayrollItem> out = new ArrayList<>();
                while (rs.next()) out.add(mapItem(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.listItems error", e);
        }
    }

    /**
     * Upsert một item theo (run_id, user_id).
     * UPDATE trước, nếu 0 rows thì INSERT.
     * gross = base + allowance + ot + bonus - penalty - leaveDeduct
     * net   = gross - insurance - tax
     */
    public void upsertItem(PayrollItem p) {
        if (p == null || p.runId == null || p.userId == null)
            throw new IllegalArgumentException("PayrollItem thiếu runId/userId");

        // Tính gross/net từ các trường hiện có
        BigDecimal gross = nz(p.baseSalary)
                .add(nz(p.allowance))
                .add(nz(p.otPay))
                .add(nz(p.bonus))
                .subtract(nz(p.penalty))
                .subtract(nz(p.leaveDeduct));
        BigDecimal net = gross
                .subtract(nz(p.insurance))
                .subtract(nz(p.tax));

        p.gross = gross;
        p.net   = net;

        final String SQL_UPDATE =
            "UPDATE payroll_items SET base_salary=?, ot_amount=?, bonus=?, fine=?, leave_deduct=?, gross=?, net=?, note=?, updated_at=? " +
            "WHERE run_id=? AND user_id=?";
        final String SQL_INSERT =
            "INSERT INTO payroll_items(run_id,user_id,base_salary,ot_amount,bonus,fine,leave_deduct,gross,net,note,updated_at) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        LocalDateTime now = LocalDateTime.now();

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            int changed;
            try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                ps.setBigDecimal(1,  nz(p.baseSalary));
                ps.setBigDecimal(2,  nz(p.otPay));
                ps.setBigDecimal(3,  nz(p.bonus));
                ps.setBigDecimal(4,  nz(p.penalty));
                ps.setBigDecimal(5,  nz(p.leaveDeduct));
                ps.setBigDecimal(6,  nz(p.gross));
                ps.setBigDecimal(7,  nz(p.net));
                ps.setString(8,  p.note);
                ps.setTimestamp(9,  Timestamp.valueOf(now));
                ps.setLong(10, p.runId);
                ps.setLong(11, p.userId);
                changed = ps.executeUpdate();
            }
            if (changed == 0) {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
                    ps.setLong(1,  p.runId);
                    ps.setLong(2,  p.userId);
                    ps.setBigDecimal(3,  nz(p.baseSalary));
                    ps.setBigDecimal(4,  nz(p.otPay));
                    ps.setBigDecimal(5,  nz(p.bonus));
                    ps.setBigDecimal(6,  nz(p.penalty));
                    ps.setBigDecimal(7,  nz(p.leaveDeduct));
                    ps.setBigDecimal(8,  nz(p.gross));
                    ps.setBigDecimal(9,  nz(p.net));
                    ps.setString(10, p.note);
                    ps.setTimestamp(11, Timestamp.valueOf(now));
                    ps.executeUpdate();
                }
            }
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.upsertItem error", e);
        }
    }

    /** Tìm id kỳ lương theo (year, month). */
    public Optional<Long> findRun(int y, int m) {
        final String SQL = "SELECT id FROM payroll_runs WHERE [year]=? AND [month]=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setInt(1, y);
            ps.setInt(2, m);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getLong("id"));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.findRun error", e);
        }
    }
}
