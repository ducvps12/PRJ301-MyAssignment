/*
 * User model – tương thích với RoleFilter & AuthServlet
 * Hỗ trợ role: ADMIN | DIV_LEADER | TEAM_LEAD | QA_LEAD | STAFF
 */
package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== Role constants =====
    public static final String ROLE_ADMIN      = "ADMIN";
    public static final String ROLE_DIV_LEADER = "DIV_LEADER";
    public static final String ROLE_TEAM_LEAD  = "TEAM_LEAD";
    public static final String ROLE_QA_LEAD    = "QA_LEAD";
    public static final String ROLE_STAFF      = "STAFF";

    // ===== Core fields =====
    private int id;
    private String username;
    private String password;
    private String fullName;

    private String role;        // ADMIN | DIV_LEADER | TEAM_LEAD | QA_LEAD | STAFF
    private int roleId;         // optional (0 = unknown)
    private int departmentId;   // optional (0 = unknown)
    private String department;  // IT | QA | SALE ...

    // ===== Contact & profile =====
    private String email;
    private String phone;
    private String address;
    private LocalDate birthday;
    private String bio;
    private String avatarUrl;

    // ===== Status & timestamps =====
    private int status = 1;     // 1 = active, 0 = locked
    private Date createdAt;
    private Date updatedAt;

    public User() {}

    public User(int id, String username, String fullName, String role, String department) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
    }

    // ===== Getters / Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return id; }              // alias
    public void setUserId(int id) { this.id = id; }    // alias

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    /** Set role bằng code (VARCHAR). */
    public void setRole(String role) {
        this.role = role;
        // nếu có bảng map id ↔ code thì cập nhật roleId tương ứng (nếu biết)
        this.roleId = mapRoleCodeToId(role);
    }
    /** Overload: Set role bằng roleId (INT) – tiện cho ResultSet.getObject(). */
    public void setRole(Integer roleId) {
        setRoleId(roleId);
        this.role = mapRoleIdToCode(this.roleId);
    }

    /** Role code chuẩn hoá (upper + fallback STAFF). */
    public String getRoleCode() {
        String r = (role == null || role.isBlank()) ? ROLE_STAFF : role;
        return r.trim().toUpperCase();
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    /** Null-safe overload. */
    public void setRoleId(Integer roleId) { this.roleId = (roleId == null) ? 0 : roleId; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    /** Null-safe overload. */
    public void setDepartmentId(Integer departmentId) { this.departmentId = (departmentId == null) ? 0 : departmentId; }

    public String getDepartment() { return department; }
    /** Set department bằng tên. */
    public void setDepartment(String department) { this.department = department; }
    /** Overload: nhận departmentId (INT). */
    public void setDepartment(Integer departmentId) { setDepartmentId(departmentId); }
    public String getDeptName() { return department; }     // alias
    public void setDeptName(String deptName) { this.department = deptName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public boolean isActive() { return status == 1; }
    public String getStatusText() { return isActive() ? "ACTIVE" : "INACTIVE"; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // ===== Helpers for authorization =====
    public boolean hasRole(String r) {
        return getRoleCode().equalsIgnoreCase(r);
    }
    public boolean hasAnyRole(String... roles) {
        String rc = getRoleCode();
        return Arrays.stream(roles).anyMatch(r -> rc.equalsIgnoreCase(r));
    }
    public boolean isAdmin() { return hasRole(ROLE_ADMIN); }

    /** Leader: DIV_LEADER | TEAM_LEAD | QA_LEAD | (hậu tố *_LEAD/*_LEADER) */
    public boolean isLead() {
    String rc = getRoleCode();
    if (hasAnyRole(ROLE_DIV_LEADER, ROLE_TEAM_LEAD, ROLE_QA_LEAD)) return true;
    return rc.endsWith("_LEAD") || rc.endsWith("_LEADER")
        || rc.equals("LEADER") || rc.equals("MANAGER"); // linh hoạt
}

/** Alias theo tên method mà RoleFilter/Servlet đang dùng. */
public boolean isLeader() { return isLead(); }

public boolean isStaff() { return hasAnyRole(ROLE_STAFF, "EMPLOYEE"); }

public boolean canAccessAdminDashboard() { return isAdmin() || isLead(); }
public boolean canAccessAdminUsers()     { return isAdmin(); }
public boolean canApproveRequests()      { return isAdmin() || isLead(); }


    // ===== Display / Debug =====
    public String getDisplayName() {
        return (fullName != null && !fullName.isBlank()) ? fullName : username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id == user.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "User{id=" + id +
                ", username='" + username + '\'' +
                ", role='" + getRoleCode() + '\'' +
                ", roleId=" + roleId +
                ", departmentId=" + departmentId +
                ", department='" + department + '\'' +
                ", status=" + status +
                '}';
    }

    // ===== Mapping helpers =====
    /** Map roleId (INT) -> role code (VARCHAR). Điều chỉnh theo DB của bạn. */
    private static String mapRoleIdToCode(int id) {
        switch (id) {
            case 1: return ROLE_ADMIN;
            case 2: return ROLE_DIV_LEADER;
            case 3: return ROLE_TEAM_LEAD;
            case 4: return ROLE_QA_LEAD;
            case 5: return ROLE_STAFF;
            default: return null; // unknown
        }
    }
    /** Map role code (VARCHAR) -> roleId (INT). Điều chỉnh theo DB của bạn. */
    private static int mapRoleCodeToId(String code) {
        if (code == null) return 0;
        switch (code.trim().toUpperCase()) {
            case ROLE_ADMIN:      return 1;
            case ROLE_DIV_LEADER: return 2;
            case ROLE_TEAM_LEAD:  return 3;
            case ROLE_QA_LEAD:    return 4;
            case ROLE_STAFF:      return 5;
            default: return 0;
        }
    }
}
