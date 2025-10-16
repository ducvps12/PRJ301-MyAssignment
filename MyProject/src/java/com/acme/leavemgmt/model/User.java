/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.acme.leavemgmt.model;

/**
 *
 * @author mtien
 */

public class User {
    private int id;
    private String username;
    private String password; // demo để đơn giản, đi thi có thể hash
    private String fullName;
    private String role;       // EMPLOYEE | MANAGER | LEADER
    private String department; // IT | QA | Sale...

    public User() {}

    public User(int id, String username, String fullName, String role, String department) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public boolean isManager() { return "MANAGER".equalsIgnoreCase(role); }
    public boolean isLeader()  { return "LEADER".equalsIgnoreCase(role); }
}
