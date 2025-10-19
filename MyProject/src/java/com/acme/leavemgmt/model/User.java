/*
 * User model – tương thích cả getRole() và getRoleCode()
 */
package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private int id;
    private String username;
    private String password;   // demo: có thể hash ở tầng DAO/service
    private String fullName;
    private String role;       // EMPLOYEE | MANAGER | LEADER | ADMIN
    private String department; // IT | QA | Sale...

    public User() {}

    public User(int id, String username, String fullName, String role, String department) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
    }

    // ===== getters & setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Alias để tương thích với một số DAO/Servlet dùng getUserId()
    public int getUserId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Chuẩn hoá role về UPPER_CASE; mặc định EMPLOYEE nếu null/rỗng
    public String getRoleCode() {
        String r = (role == null || role.isBlank()) ? "EMPLOYEE" : role;
        return r.toUpperCase();
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    // ===== helpers tiện dụng ở view/logic =====
    public boolean isAdmin()   { return "ADMIN".equalsIgnoreCase(getRoleCode()); }
    public boolean isManager() { return "MANAGER".equalsIgnoreCase(getRoleCode()); }
    public boolean isLeader()  { return "LEADER".equalsIgnoreCase(getRoleCode()); }
    public boolean isEmployee(){ return "EMPLOYEE".equalsIgnoreCase(getRoleCode()); }

    public String getDeptName() { return department; } // alias nếu nơi khác dùng dept_name
    public void setDeptName(String deptName) { this.department = deptName; }

    public String getDisplayName() {
        return (fullName != null && !fullName.isBlank()) ? fullName : username;
    }

    // Optional: equals/hashCode nếu cần lưu vào Set/Map theo id
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
        return "User{id=" + id + ", username='" + username + "', role='" + getRoleCode() +
               "', department='" + department + "'}";
    }
}
