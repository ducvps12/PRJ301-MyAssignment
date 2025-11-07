/*
 * User model – tương thích RoleFilter & Admin/HR
 * Hỗ trợ cả tên thuộc tính cũ: full_name/department, và mới: fullname/divisionName.
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

    /** Tên chuẩn camelCase (alias: full_name, fullName) */
    private String fullname;

    /** role code (ADMIN/STAFF/...) – alias qua roleId */
    private String role;
    private int roleId;

    /** Division/Department mapping (dùng chung) */
    private Integer divisionId;     // id phòng/khối
    private String  divisionName;   // tên phòng/khối (alias của department)

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

    private int accountStatus = 1;  // 1 = ACTIVE
    private String empStatus;       // ACTIVE/INACTIVE... (ưu tiên hiển thị)

    // ===== Timestamps =====
    private Date createdAt;
    private Date updatedAt;

    // ===== Login tracking =====
    private Date lastLogin;
    private String lastIp;

    // ===== OAuth (optional) =====
    private String authProvider;

    // ===== Display-only names (JOIN từ DAO) =====
    private String managerName;
    private String roleName;
    private String departmentName; // = divisionName (alias)
    private int managerId;

    public User() {}

    public User(int id, String username, String fullname, String role, String divisionName) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.role = role;
        this.divisionName = divisionName;
        this.roleId = mapRoleCodeToId(role);
    }

    // ===== Getters / Setters (JavaBeans) =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    /** alias cho một số DAO cũ */
    public int getUserId() { return id; }
    public void setUserId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // ---- fullname (và alias tương thích) ----
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getFullName() { return fullname; }
    public void setFullName(String fullName) { this.fullname = fullName; }
    public String getFull_name() { return fullname; }
    public void setFull_name(String full_name) { this.fullname = full_name; }

    // ---- role / roleId (tự map qua lại) ----
    public String getRole() { return role; }
    public void setRole(String role) {
        this.role = role;
        this.roleId = mapRoleCodeToId(role);
    }
    /** Cho mapper cũ set role bằng số */
    public void setRole(Integer roleId) {
        this.roleId = (roleId == null) ? 0 : roleId;
        this.role = mapRoleIdToCode(this.roleId);
    }

    public String getRoleCode() {
        String r = (role == null || role.isBlank()) ? ROLE_STAFF : role;
        return r.trim().toUpperCase();
    }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) {
        this.roleId = roleId;
        this.role = mapRoleIdToCode(roleId);
    }

    // ---- division/department unify ----
    public Integer getDivisionId() { return divisionId; }
    public void setDivisionId(Integer divisionId) { this.divisionId = divisionId; }
    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
        this.departmentName = divisionName; // giữ đồng bộ alias
    }

    /** ==== ALIASES cho "department" (giữ tương thích) ==== */
    public int getDepartmentId() { return divisionId == null ? 0 : divisionId; }
    public void setDepartmentId(int departmentId) { this.divisionId = departmentId; }
    public void setDepartmentId(Integer departmentId) { this.divisionId = departmentId; }
    public String getDepartment() { return divisionName; }
    public void setDepartment(String department) {
        this.divisionName = department;
        this.departmentName = department;
    }

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
    public void setAccountStatus(int status) { this.accountStatus = status; }
    /** Giữ alias cũ nhưng tránh mơ hồ với JavaBeans (gọi vào setAccountStatus) */
    @Deprecated public void setStatus(int status) { setAccountStatus(status); }

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

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    // ===== Display helpers for JSP/UI =====
    public String getDisplayName() {
        return (fullname != null && !fullname.isBlank()) ? fullname : username;
    }
    public boolean hasLastLogin() { return lastLogin != null; }

    /** Dùng cho <fmt:formatDate value="${u.createdAtDate}"> */
    public Date getCreatedAtDate() { return createdAt; }

    /** Dùng cho <fmt:formatDate value="${u.birthdayDate}"> */
    public Date getBirthdayDate() {
        if (birthday == null) return null;
        return Date.from(birthday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Avatar ký tự đầu */
    public String getInitial() {
        if (fullname != null && !fullname.isBlank()) return fullname.substring(0,1).toUpperCase();
        if (username != null && !username.isBlank()) return username.substring(0,1).toUpperCase();
        return "?";
    }

    /** Tên vai trò hiển thị (ưu tiên roleName, rơi về role code) */
    public String getDisplayRole() {
        return (roleName != null && !roleName.isBlank()) ? roleName : getRoleCode();
    }

    /** Nhãn vị trí gộp cho UI: Division · Role · Manager */
    public String getPositionLabel() {
        String d = nz(divisionName);
        String r = nz(getDisplayRole());
        String m = nz(managerName);
        String out = d;
        if (!r.isEmpty()) out = out.isEmpty() ? r : (out + " · " + r);
        if (!m.isEmpty()) out = out.isEmpty() ? ("Manager: " + m) : (out + " · Manager: " + m);
        return out;
    }

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

    // ===== Object methods =====
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id == user.id;
    }
    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "User{id=" + id +
                ", username='" + username + '\'' +
                ", role='" + getRoleCode() + '\'' +
                ", divisionName='" + divisionName + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }

    // ===== Mapping helpers: roleId <-> role code =====
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
            case ROLE_ADMIN      -> 1;
            case ROLE_DIV_LEADER -> 2;
            case ROLE_TEAM_LEAD  -> 3;
            case ROLE_QA_LEAD    -> 4;
            case ROLE_STAFF      -> 5;
            default              -> 0;
        };
    }

    // ===== Display-name fields (JOIN từ DAO) =====
    public String getManagerName() { return nz(managerName); }
    public void setManagerName(String s) { this.managerName = n(s); }

    public String getRoleName() { return nz(roleName); }
    public void setRoleName(String s) { this.roleName = n(s); }

    public String getDepartmentName() { return nz(departmentName); }
    public void setDepartmentName(String s) {
        this.departmentName = n(s);
        // đồng bộ ngược cho alias
        if (this.departmentName != null && !this.departmentName.isBlank()) {
            this.divisionName = this.departmentName;
        }
    }

    // ===== helpers =====
    private static String n(String s){ return s==null?null:s.trim(); }
    private static String nz(String s){ return s==null?"":s.trim(); }

    
public Integer getManagerId() { return managerId; }
public void setManagerId(int managerId) { this.managerId = managerId; }      // <-- FIX: không ném exception nữa
public void setManagerId(Integer managerId) { this.managerId = managerId; }  // optional
    
    
    
    
}
