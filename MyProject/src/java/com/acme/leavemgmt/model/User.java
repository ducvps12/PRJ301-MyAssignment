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
    private int roleId;         // optional – nếu DB có bảng Roles
    private int departmentId;   // optional – nếu DB có bảng Departments
    private String department;  // IT | QA | SALE ...

    // ===== Contact & profile =====
    private String email;
    private String phone;
    private String address;     // NEW
    private LocalDate birthday; // NEW
    private String bio;         // NEW
    private String avatarUrl;   // NEW

    // ===== Status & timestamps =====
    private int status = 1;     // 1=active, 0=locked
    private Date createdAt;     // NEW
    private Date updatedAt;     // NEW

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
    public void setRole(String role) { this.role = role; }
    public String getRoleCode() {
        String r = (role == null || role.isBlank()) ? ROLE_STAFF : role;
        return r.toUpperCase();
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDeptName() { return department; }     // alias
    public void setDeptName(String deptName) { this.department = deptName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    // ===== Implemented fields (trước đây bị UnsupportedOperationException) =====
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
    public boolean isLead() {
        String rc = getRoleCode();
        return rc.endsWith("_LEAD") || rc.endsWith("_LEADER") ||
               rc.equalsIgnoreCase("LEADER") || rc.equalsIgnoreCase("MANAGER");
    }
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
                ", departmentId=" + departmentId +
                ", department='" + department + '\'' +
                ", status=" + status +
                '}';
    }
}
