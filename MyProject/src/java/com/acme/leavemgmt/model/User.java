/*
 * User model – tương thích RoleFilter & Admin HR
 * Hỗ trợ cả tên thuộc tính cũ: fullName/department, và mới: fullname/division.
 */
package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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

    // ===== Core fields (DB) =====
    private int id;
    private String username;
    private String password;

    private String fullname;
    private String role;
    private int roleId;

    private Integer divisionId;
    private String divisionName;

    // ===== Contact & profile =====
    private String email;
    private String phone;
    private String address;
    private LocalDate birthday;
    private String bio;
    private String avatarUrl;

    private String jobTitle;
    private LocalDate joinDate;
    private LocalDate contractEnd;
    private BigDecimal salary;

    private int accountStatus = 1;
    private String empStatus;

    // ===== Timestamps =====
    private Date createdAt;
    private Date updatedAt;

    // ===== Login tracking =====
    private Date lastLogin;
    private String lastIp;

    public User() {}

    public User(int id, String username, String fullname, String role, String divisionName) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.role = role;
        this.divisionName = divisionName;
    }

    // ===== Getters / Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return id; }
    public void setUserId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getFullName() { return fullname; }
    public void setFullName(String fullName) { this.fullname = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) {
        this.role = role;
        this.roleId = mapRoleCodeToId(role);
    }
    public void setRole(Integer roleId) {
        setRoleId(roleId);
        this.role = mapRoleIdToCode(this.roleId);
    }
    public String getRoleCode() {
        String r = (role == null || role.isBlank()) ? ROLE_STAFF : role;
        return r.trim().toUpperCase();
    }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public void setRoleId(Integer roleId) { this.roleId = (roleId == null) ? 0 : roleId; }

    public Integer getDivisionId() { return divisionId; }
    public void setDivisionId(Integer divisionId) { this.divisionId = divisionId; }
    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }

    public int getDepartmentId() { return divisionId == null ? 0 : divisionId; }
    public void setDepartmentId(int departmentId) { this.divisionId = departmentId; }
    public void setDepartmentId(Integer departmentId) { this.divisionId = departmentId; }
    public String getDepartment() { return divisionName; }
    public void setDepartment(String department) { this.divisionName = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public LocalDate getContractEnd() { return contractEnd; }
    public void setContractEnd(LocalDate contractEnd) { this.contractEnd = contractEnd; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    // ===== Status =====
    public String getStatus() {
        if (empStatus != null && !empStatus.isBlank()) return empStatus;
        return accountStatus == 1 ? "ACTIVE" : "INACTIVE";
    }
    public void setStatus(String status) { this.empStatus = status; }
    public int getAccountStatus() { return accountStatus; }
    public void setStatus(int status) { this.accountStatus = status; }
    public boolean isActive() { return accountStatus == 1; }

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

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    public String getLastIp() { return lastIp; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }

    // ===== Authorization helpers =====
    public boolean hasRole(String r) { return getRoleCode().equalsIgnoreCase(r); }
    public boolean hasAnyRole(String... roles) {
        String rc = getRoleCode();
        return Arrays.stream(roles).anyMatch(r -> rc.equalsIgnoreCase(r));
    }
    public boolean isAdmin() { return hasRole(ROLE_ADMIN); }
    public boolean isLead() {
        String rc = getRoleCode();
        return hasAnyRole(ROLE_DIV_LEADER, ROLE_TEAM_LEAD, ROLE_QA_LEAD)
                || rc.endsWith("_LEAD") || rc.endsWith("_LEADER")
                || rc.equals("LEADER") || rc.equals("MANAGER");
    }
    public boolean isLeader() { return isLead(); }
    public boolean isStaff() { return hasAnyRole(ROLE_STAFF, "EMPLOYEE"); }
    public boolean canAccessAdminDashboard() { return isAdmin() || isLead(); }
    public boolean canAccessAdminUsers()     { return isAdmin(); }
    public boolean canApproveRequests()      { return isAdmin() || isLead(); }

    public String getDisplayName() {
        return (fullname != null && !fullname.isBlank()) ? fullname : username;
    }
    public boolean hasLastLogin() { return lastLogin != null; }

    // ===== Support getters for JSP formatting =====
    /** Dùng cho <fmt:formatDate value="${u.createdAtDate}"> */
    public Date getCreatedAtDate() {
        return createdAt;
    }

    /** Dùng cho <fmt:formatDate value="${u.birthdayDate}"> */
    public Date getBirthdayDate() {
        if (birthday == null) return null;
        return Date.from(birthday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Dùng cho avatar chữ cái đầu */
    public String getInitial() {
        if (fullname != null && !fullname.isBlank()) return fullname.substring(0,1).toUpperCase();
        if (username != null && !username.isBlank()) return username.substring(0,1).toUpperCase();
        return "?";
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
                ", divisionName='" + divisionName + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }

    // ===== Mapping helpers cho roleId <-> role code =====
    private static String mapRoleIdToCode(int id) {
        return switch (id) {
            case 1 -> ROLE_ADMIN;
            case 2 -> ROLE_DIV_LEADER;
            case 3 -> ROLE_TEAM_LEAD;
            case 4 -> ROLE_QA_LEAD;
            case 5 -> ROLE_STAFF;
            default -> null;
        };
    }
    private static int mapRoleCodeToId(String code) {
        if (code == null) return 0;
        return switch (code.trim().toUpperCase()) {
            case ROLE_ADMIN -> 1;
            case ROLE_DIV_LEADER -> 2;
            case ROLE_TEAM_LEAD -> 3;
            case ROLE_QA_LEAD -> 4;
            case ROLE_STAFF -> 5;
            default -> 0;
        };
    }
}
