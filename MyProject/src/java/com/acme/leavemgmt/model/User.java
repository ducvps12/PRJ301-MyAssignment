/*
 * User model – tương thích với RoleFilter & LoginServlet
 * Hỗ trợ đầy đủ các role: ADMIN | DIV_LEADER | TEAM_LEAD | QA_LEAD | STAFF
 */
package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // ===== Core fields =====
    private int id;
    private String username;
    private String password;
    private String fullName;

    // Quyền & phòng ban (id + name)
    private String role;            // ADMIN | DIV_LEADER | TEAM_LEAD | QA_LEAD | STAFF
    private int roleId;             // dùng khi map DB (nếu có bảng Roles)
    private int departmentId;       // dùng khi map DB (nếu có bảng Departments)
    private String department;      // tên phòng ban (IT | QA | Sales ...)

    // Thông tin liên hệ
    private String email;
    private String phone;

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

    // Alias tương thích với DAO/Servlet cũ
    public int getUserId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Chuẩn hoá role về UPPER_CASE; mặc định STAFF nếu null/rỗng
    public String getRoleCode() {
        String r = (role == null || role.isBlank()) ? "STAFF" : role;
        return r.toUpperCase();
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    // alias cho name phòng ban
    public String getDeptName() { return department; }
    public void setDeptName(String deptName) { this.department = deptName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // ===== Helpers cho phân quyền =====
    public boolean hasRole(String r) {
        return getRoleCode().equalsIgnoreCase(r);
    }

    public boolean hasAnyRole(String... roles) {
        String rc = getRoleCode();
        return Arrays.stream(roles).anyMatch(r -> rc.equalsIgnoreCase(r));
    }

    public boolean isAdmin() { return hasRole("ADMIN"); }

    // "Lead" bao gồm DIV_LEADER / TEAM_LEAD / QA_LEAD / LEADER / MANAGER
    public boolean isLead() {
        String rc = getRoleCode();
        return rc.endsWith("_LEAD") || rc.endsWith("_LEADER") ||
               rc.equals("LEADER") || rc.equals("MANAGER");
    }

    public boolean isStaff() { return hasAnyRole("STAFF", "EMPLOYEE"); }

    // Quyền cụ thể cho từng module
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
                ", departmentId=" + departmentId +
                ", department='" + department + '\'' +
                '}';
    }
}
