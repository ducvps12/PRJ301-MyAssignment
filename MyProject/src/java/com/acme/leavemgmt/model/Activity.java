package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

/**
 * Activity – bản ghi audit hành động của người dùng.
 * Mapping 1-1 với bảng dbo.User_Activity.
 */
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    // ===== Column constants (giúp DAO map an toàn) =====
    public static final String COL_ID          = "id";
    public static final String COL_USER_ID     = "user_id";
    public static final String COL_ACTION      = "action";
    public static final String COL_ENTITY_TYPE = "entity_type";
    public static final String COL_ENTITY_ID   = "entity_id";
    public static final String COL_NOTE        = "note";
    public static final String COL_IP_ADDR     = "ip_addr";
    public static final String COL_USER_AGENT  = "user_agent";
    public static final String COL_CREATED_AT  = "created_at";
    // Optional join alias:
    public static final String COL_USER_NAME   = "user_name";

    // ===== Fields =====
    private int id;
    private Integer userId;
    private String userName;     // JOIN từ Users (phục vụ hiển thị)
    private String action;       // LOGIN, APPROVE_REQUEST, ...
    private String entityType;   // REQUEST, USER, DIVISION, ...
    private Integer entityId;    // ID đối tượng
    private String note;         // ghi chú
    private String ipAddr;
    private String userAgent;
    private Date createdAt;      // java.util.Date để fmt:formatDate JSP hoạt động

    public Activity() {} // no-args for frameworks

    // ==== Convenience constructor (không bắt buộc) ====
    public Activity(int id, Integer userId, String userName, String action,
                    String entityType, Integer entityId, String note,
                    String ipAddr, String userAgent, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.note = note;
        this.ipAddr = ipAddr;
        this.userAgent = userAgent;
        setCreatedAt(createdAt); // dùng setter để copy Date
    }

    // ===== Factory từ ResultSet (tiện dùng trong DAO) =====
    public static Activity from(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setId(rs.getInt(COL_ID));

        int uid = rs.getInt(COL_USER_ID);
        a.setUserId(rs.wasNull() ? null : uid);

        a.setUserName(safe(rs, COL_USER_NAME)); // có thể null nếu không join
        a.setAction(safe(rs, COL_ACTION));
        a.setEntityType(safe(rs, COL_ENTITY_TYPE));

        int eid = rs.getInt(COL_ENTITY_ID);
        a.setEntityId(rs.wasNull() ? null : eid);

        a.setNote(safe(rs, COL_NOTE));
        a.setIpAddr(safe(rs, COL_IP_ADDR));
        a.setUserAgent(safe(rs, COL_USER_AGENT));

        // Timestamp -> Date
        java.sql.Timestamp ts = rs.getTimestamp(COL_CREATED_AT);
        a.setCreatedAt(ts == null ? null : new Date(ts.getTime()));

        return a;
    }
    private static String safe(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return null; }
    }

    // ===== Getters/Setters (Date phòng thủ) =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getIpAddr() { return ipAddr; }
    public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Date getCreatedAt() { return createdAt == null ? null : new Date(createdAt.getTime()); }
    public void setCreatedAt(Date createdAt) { this.createdAt = (createdAt == null ? null : new Date(createdAt.getTime())); }

    // ===== Alias cho code cũ (tùy chọn) =====
    public String getIp() { return getIpAddr(); }
    public void setIp(String ip) { setIpAddr(ip); }

    public String getUa() { return getUserAgent(); }
    public void setUa(String ua) { setUserAgent(ua); }

    public String getUsername() { return getUserName(); }
    public void setUsername(String username) { setUserName(username); }

    // ===== View helper =====
    public String getEntityDisplay() {
        if (entityType == null || entityType.isEmpty()) return "";
        return entityType + (entityId != null ? (" #" + entityId) : "");
    }

    // ===== Object overrides =====
    @Override public String toString() {
        return "Activity{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", note='" + note + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity)) return false;
        Activity a = (Activity) o;
        return id == a.id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
