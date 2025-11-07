package com.acme.leavemgmt.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;

public class PayrollDAO {

    private final DataSource ds;
    public PayrollDAO(DataSource ds) { this.ds = ds; }

    // ======================= Model =======================
    public static class PayrollItem {
        public Long id;
        public Long runId;
        public Long userId;

        public BigDecimal baseSalary = BigDecimal.ZERO;
        public BigDecimal allowance  = BigDecimal.ZERO;
        public BigDecimal otPay      = BigDecimal.ZERO;
        public BigDecimal bonus      = BigDecimal.ZERO;
        public BigDecimal penalty    = BigDecimal.ZERO; // tiền phạt
        public BigDecimal insurance  = BigDecimal.ZERO; // bảo hiểm
        public BigDecimal tax        = BigDecimal.ZERO; // thuế
        public BigDecimal netPay     = BigDecimal.ZERO; // lưu vào cột net_pay

        public String note;
        public LocalDateTime updatedAt;

        // getters (tiện cho JSP)
        public Long getUserId(){ return userId; }
        public BigDecimal getBaseSalary(){ return baseSalary; }
        public BigDecimal getAllowance(){ return allowance; }
        public BigDecimal getOtPay(){ return otPay; }
        public BigDecimal getBonus(){ return bonus; }
        public BigDecimal getPenalty(){ return penalty; }
        public BigDecimal getInsurance(){ return insurance; }
        public BigDecimal getTax(){ return tax; }
        public BigDecimal getNetPay(){ return netPay; }
        public String getNote(){ return note; }
        public Long getRunId(){ return runId; }
        public Long getId(){ return id; }
        public LocalDateTime getUpdatedAt(){ return updatedAt; }

        // setters
        public void setRunId(long runId)          { this.runId = runId; }
        public void setRunId(Long runId)          { this.runId = runId; }
        public void setUserId(Long userId)        { this.userId = userId; }
        public void setBaseSalary(BigDecimal v)   { this.baseSalary = v == null ? BigDecimal.ZERO : v; }
        public void setAllowance(BigDecimal v)    { this.allowance  = v == null ? BigDecimal.ZERO : v; }
        public void setOtPay(BigDecimal v)        { this.otPay      = v == null ? BigDecimal.ZERO : v; }
        public void setBonus(BigDecimal v)        { this.bonus      = v == null ? BigDecimal.ZERO : v; }
        public void setPenalty(BigDecimal v)      { this.penalty    = v == null ? BigDecimal.ZERO : v; }
        public void setInsurance(BigDecimal v)    { this.insurance  = v == null ? BigDecimal.ZERO : v; }
        public void setTax(BigDecimal v)          { this.tax        = v == null ? BigDecimal.ZERO : v; }
        public void setNetPay(BigDecimal v)       { this.netPay     = v == null ? BigDecimal.ZERO : v; }
        public void setNote(String note)          { this.note = (note == null || note.isBlank()) ? null : note.trim(); }
    }

    // ======================= Utils =======================
    private static BigDecimal nz(BigDecimal d){ return d == null ? BigDecimal.ZERO : d; }
    private static BigDecimal bz(Object o){
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number)     return BigDecimal.valueOf(((Number) o).doubleValue());
        try { return new BigDecimal(String.valueOf(o)); } catch (Exception e){ return BigDecimal.ZERO; }
    }

    /** Lấy timestamp an toàn: ưu tiên updated_at, fallback created_at nếu tồn tại. */
    private static Timestamp safeTs(ResultSet rs) {
        try {
            // nếu có cột updated_at
            rs.findColumn("updated_at");
            return rs.getTimestamp("updated_at");
        } catch (SQLException ignore) {
            try {
                // nếu có cột created_at
                rs.findColumn("created_at");
                return rs.getTimestamp("created_at");
            } catch (SQLException ignore2) {
                return null;
            }
        }
    }

    private PayrollItem mapItem(ResultSet rs) throws SQLException {
        PayrollItem p = new PayrollItem();
        p.id         = rs.getLong("id");
        p.runId      = rs.getLong("run_id");
        p.userId     = rs.getLong("user_id");
        p.baseSalary = nz(rs.getBigDecimal("base_salary"));
        p.allowance  = nz(rs.getBigDecimal("allowance"));
        // hai tên cột OT phổ biến: ot_pay hoặc ot_amount
        BigDecimal ot = null;
        try { rs.findColumn("ot_pay");    ot = rs.getBigDecimal("ot_pay"); } catch (SQLException ignore) {}
        if (ot == null) { try { rs.findColumn("ot_amount"); ot = rs.getBigDecimal("ot_amount"); } catch (SQLException ignore) {} }
        p.otPay      = nz(ot);
        p.bonus      = nz(rs.getBigDecimal("bonus"));
        p.penalty    = nz(rs.getBigDecimal("penalty"));
        p.insurance  = nz(rs.getBigDecimal("insurance"));
        p.tax        = nz(rs.getBigDecimal("tax"));
        p.netPay     = nz(rs.getBigDecimal("net_pay"));
        p.note       = null;
        try { rs.findColumn("note"); p.note = rs.getString("note"); } catch (SQLException ignore) {}
        Timestamp ts = safeTs(rs);
        p.updatedAt  = (ts == null ? null : ts.toLocalDateTime());
        return p;
    }

    // ======================= RUNS =======================

    /** Tạo kỳ lương nếu chưa có; trả về id kỳ */
    public long createRun(int year, int month) {
        Optional<Long> existed = findRun(year, month);
        if (existed.isPresent()) return existed.get();

        final String SQL = "INSERT INTO dbo.payroll_runs(period_year, period_month, locked, created_at) VALUES(?,?,0,?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return findRun(year, month).orElseThrow(() -> new RuntimeException("Cannot obtain payroll run id"));
        } catch (SQLException e) {
            Optional<Long> fid = findRun(year, month);
            if (fid.isPresent()) return fid.get();
            throw new RuntimeException("PayrollDAO.createRun error", e);
        }
    }

    /** Khóa kỳ lương */
    public boolean lockRun(long runId) {
        final String SQL = "UPDATE dbo.payroll_runs SET locked=1 WHERE id=? AND (locked=0 OR locked IS NULL)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setLong(1, runId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.lockRun error", e);
        }
    }

    /** Tìm id kỳ lương theo (year, month) */
    public Optional<Long> findRun(int year, int month) {
        final String SQL = "SELECT id FROM dbo.payroll_runs WHERE period_year=? AND period_month=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getLong("id"));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.findRun error", e);
        }
    }

    // ======================= ITEMS =======================

    /** Danh sách item dạng Map (dùng cho JSP linh hoạt) */
    public List<Map<String,Object>> listItems(long runId) {
        final String SQL =
            "SELECT i.*, u.full_name " +
            "FROM dbo.payroll_items i " +
            "LEFT JOIN dbo.Users u ON u.id = i.user_id " +
            "WHERE i.run_id=? ORDER BY u.full_name, i.user_id";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setLong(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String,Object>> out = new ArrayList<>();
                while (rs.next()) {
                    PayrollItem p = mapItem(rs);
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("id", p.id);
                    m.put("run_id", p.runId);
                    m.put("user_id", p.userId);
                    m.put("full_name", rs.getString("full_name"));
                    m.put("base_salary", p.baseSalary);
                    m.put("allowance",   p.allowance);
                    m.put("ot_pay",      p.otPay);
                    m.put("bonus",       p.bonus);
                    m.put("penalty",     p.penalty);
                    m.put("insurance",   p.insurance);
                    m.put("tax",         p.tax);
                    m.put("net_pay",     p.netPay);
                    m.put("note",        p.note);
                    m.put("updated_at",  p.updatedAt);
                    out.add(m);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.listItems error", e);
        }
    }

    /** Danh sách item dạng đối tượng (Servlet dùng List<PayrollItem>) */
public List<PayrollItem> listItemsAsObjects(long runId) {
    // CHỈ dùng cột thật sự tồn tại trong bảng của bạn
    final String SQL =
        "SELECT id, run_id, user_id, base_salary, allowance, ot_pay, " +
        "       bonus, penalty, insurance, tax, net_pay, note " +   // KHÔNG nhắc tới updated_at
        "FROM dbo.payroll_items WHERE run_id=? ORDER BY user_id";

    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(SQL)) {
        ps.setLong(1, runId);
        try (ResultSet rs = ps.executeQuery()) {
            List<PayrollItem> out = new ArrayList<>();
            while (rs.next()) out.add(mapItem(rs));  // mapItem đã tự dò updated_at/created_at nếu có
            return out;
        }
    } catch (SQLException e) {
        throw new RuntimeException("PayrollDAO.listItemsAsObjects error", e);
    }
}

    /**
     * Upsert 1 item theo (run_id, user_id).
     * gross = base + allowance + ot + bonus - penalty
     * net   = gross - insurance - tax  -> ghi vào cột net_pay
     */
    public void upsertItem(PayrollItem p) {
        if (p == null || p.runId == null || p.userId == null)
            throw new IllegalArgumentException("PayrollItem thiếu runId/userId");

        BigDecimal gross = nz(p.baseSalary)
                .add(nz(p.allowance))
                .add(nz(p.otPay))
                .add(nz(p.bonus))
                .subtract(nz(p.penalty));

        BigDecimal net = gross
                .subtract(nz(p.insurance))
                .subtract(nz(p.tax));

        p.netPay = net;
        LocalDateTime now = LocalDateTime.now();

        final String SQL_UPDATE =
            "UPDATE dbo.payroll_items " +
            "SET base_salary=?, allowance=?, ot_pay=?, bonus=?, penalty=?, " +
            "    insurance=?, tax=?, net_pay=?, note=?, updated_at=? " +
            "WHERE run_id=? AND user_id=?";

        final String SQL_INSERT =
            "INSERT INTO dbo.payroll_items(" +
            " run_id, user_id, base_salary, allowance, ot_pay, bonus, penalty, insurance, tax, net_pay, note, updated_at" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);

            int changed;
            try (PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {
                ps.setBigDecimal(1, nz(p.baseSalary));
                ps.setBigDecimal(2, nz(p.allowance));
                ps.setBigDecimal(3, nz(p.otPay));
                ps.setBigDecimal(4, nz(p.bonus));
                ps.setBigDecimal(5, nz(p.penalty));
                ps.setBigDecimal(6, nz(p.insurance));
                ps.setBigDecimal(7, nz(p.tax));
                ps.setBigDecimal(8, nz(p.netPay));
                ps.setString(9, p.note);
                ps.setTimestamp(10, Timestamp.valueOf(now));
                ps.setLong(11, p.runId);
                ps.setLong(12, p.userId);
                changed = ps.executeUpdate();
            }

            if (changed == 0) {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT)) {
                    ps.setLong(1,  p.runId);
                    ps.setLong(2,  p.userId);
                    ps.setBigDecimal(3,  nz(p.baseSalary));
                    ps.setBigDecimal(4,  nz(p.allowance));
                    ps.setBigDecimal(5,  nz(p.otPay));
                    ps.setBigDecimal(6,  nz(p.bonus));
                    ps.setBigDecimal(7,  nz(p.penalty));
                    ps.setBigDecimal(8,  nz(p.insurance));
                    ps.setBigDecimal(9,  nz(p.tax));
                    ps.setBigDecimal(10, nz(p.netPay));
                    ps.setString(11, p.note);
                    ps.setTimestamp(12, Timestamp.valueOf(now));
                    ps.executeUpdate();
                }
            }

            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("PayrollDAO.upsertItem error", e);
        }
    }

    // tiện ích nhanh để build Map -> PayrollItem
    public static PayrollItem fromMap(long runId, long userId, Map<String,Object> m){
        PayrollItem p = new PayrollItem();
        p.runId     = runId;
        p.userId    = userId;
        p.baseSalary= bz(m.get("base_salary"));
        p.allowance = bz(m.get("allowance"));
        p.otPay     = bz(m.get("ot_pay"));
        p.bonus     = bz(m.get("bonus"));
        p.penalty   = bz(m.get("penalty"));
        p.insurance = bz(m.get("insurance"));
        p.tax       = bz(m.get("tax"));
        p.note      = (String) m.getOrDefault("note", null);
        return p;
    }
}
