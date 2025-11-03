/*
 * User model – tương thích RoleFilter & Admin HR
 * Hỗ trợ cả tên thuộc tính cũ: fullName/department, và mới: fullname/division.
 */
package com.acme.leavemgmt.model;

import java.io.Serializable;
import java.math.BigDecimal;
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

    // ===== Core fields (DB) =====
    private int id;
    private String username;
    private String password;

    /** DB column: fullname (giữ thêm alias fullName cho code cũ) */
    private String fullname;

    /** DB column: role (code) + tùy chọn roleId để map bảng Roles nếu cần */
    private String role;
    private int roleId;               // optional

    /** DB column: division_id + alias departmentId/departmentName để tương thích */
    private Integer divisionId;       // nullable
    private String divisionName;

    // ===== Contact & profile =====
    private String email;
    private String phone;
    private String address;
    private LocalDate birthday;
    private String bio;
    private String avatarUrl;

    /** DB column: job_title */
    private String jobTitle;

    /** DB column: join_date, contract_end */
    private LocalDate joinDate;
    private LocalDate contractEnd;

    /** DB column: salary */
    private BigDecimal salary;

    /**
     * Trạng thái:
     * - accountStatus: dạng số (1=active/0=locked) – dùng cho auth cũ
     * - empStatus:     dạng chuỗi (ACTIVE/ON_LEAVE/RESIGNED/TERMINATED) – dùng cho HR
     *
     * Getter getStatus() trả về empStatus nếu có, ngược lại suy từ accountStatus.
     */
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

    // ===== Getters / Setters (đồng bộ với DAO) =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return id; }              // alias
    public void setUserId(int id) { this.id = id; }    // alias

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // fullname + alias fullName
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getFullName() { return fullname; }       // alias cũ
    public void setFullName(String fullName) { this.fullname = fullName; }

    public String getRole() { return role; }
    /** Set role bằng code (VARCHAR). */
    public void setRole(String role) {
        this.role = role;
        this.roleId = mapRoleCodeToId(role);
    }
    /** Overload: Set role bằng roleId (INT). */
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

    // division + alias department
    public Integer getDivisionId() { return divisionId; }
    public void setDivisionId(Integer divisionId) { this.divisionId = divisionId; }
    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }

    // Aliases cho code cũ dùng department*
    public int getDepartmentId() { return divisionId == null ? 0 : divisionId; }
    public void setDepartmentId(int departmentId) { this.divisionId = departmentId; }
    public void setDepartmentId(Integer departmentId) { this.divisionId = departmentId; }
    public String getDepartment() { return divisionName; }
    public void setDepartment(String department) { this.divisionName = department; }
    public String getDeptName() { return divisionName; }
    public void setDeptName(String deptName) { this.divisionName = deptName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // HR fields
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public LocalDate getContractEnd() { return contractEnd; }
    public void setContractEnd(LocalDate contractEnd) { this.contractEnd = contractEnd; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    // Status (String/Int đồng thời)
    public String getStatus() {
        if (empStatus != null && !empStatus.isBlank()) return empStatus;
        return accountStatus == 1 ? "ACTIVE" : "INACTIVE";
    }
    public void setStatus(String status) { this.empStatus = status; }
    public int getAccountStatus() { return accountStatus; }
    public void setStatus(int status) { this.accountStatus = status; } // giữ tương thích
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

    // ===== Helpers for authorization =====
    public boolean hasRole(String r) { return getRoleCode().equalsIgnoreCase(r); }
    public boolean hasAnyRole(String... roles) {
        String rc = getRoleCode();
        return Arrays.stream(roles).anyMatch(r -> rc.equalsIgnoreCase(r));
    }
    public boolean isAdmin() { return hasRole(ROLE_ADMIN); }
    /** Leader: DIV_LEADER | TEAM_LEAD | QA_LEAD | hậu tố *_LEAD/*_LEADER */
    public boolean isLead() {
        String rc = getRoleCode();
        if (hasAnyRole(ROLE_DIV_LEADER, ROLE_TEAM_LEAD, ROLE_QA_LEAD)) return true;
        return rc.endsWith("_LEAD") || rc.endsWith("_LEADER")
                || rc.equals("LEADER") || rc.equals("MANAGER");
    }
    public boolean isLeader() { return isLead(); }
    public boolean isStaff() { return hasAnyRole(ROLE_STAFF, "EMPLOYEE"); }
    public boolean canAccessAdminDashboard() { return isAdmin() || isLead(); }
    public boolean canAccessAdminUsers()     { return isAdmin(); }
    public boolean canApproveRequests()      { return isAdmin() || isLead(); }

    // ===== Display / Debug =====
    public String getDisplayName() {
        return (fullname != null && !fullname.isBlank()) ? fullname : username;
    }
    public boolean hasLastLogin() { return lastLogin != null; }

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
                ", divisionId=" + divisionId +
                ", divisionName='" + divisionName + '\'' +
                ", status='" + getStatus() + '\'' +
                ", lastLogin=" + lastLogin +
                ", lastIp='" + lastIp + '\'' +
                '}';
    }

    // ===== Mapping helpers cho roleId <-> role code (tùy DB) =====
    private static String mapRoleIdToCode(int id) {
        switch (id) {
            case 1: return ROLE_ADMIN;
            case 2: return ROLE_DIV_LEADER;
            case 3: return ROLE_TEAM_LEAD;
            case 4: return ROLE_QA_LEAD;
            case 5: return ROLE_STAFF;
            default: return null;
        }
    }
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
