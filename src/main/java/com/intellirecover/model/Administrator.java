package com.intellirecover.model;

import java.time.LocalDateTime;

/**
 * Represents a campus administrator or security personnel.
 * Demonstrates role-specific polymorphism.
 */
public class Administrator extends User {
    private String staffId;
    private String department;

    public Administrator(String id, String username, String passwordHash, String salt,
                         String fullName, String email, LocalDateTime createdAt,
                         String staffId, String department) {
        super(id, username, passwordHash, salt, fullName, email, UserRole.ADMINISTRATOR, createdAt);
        this.staffId = staffId;
        this.department = department;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getRoleHeader() {
        return String.format("ADMINISTRATOR CONTROL PANEL | %s (Staff ID: %s, Dept: %s)",
                getFullName(), staffId, department);
    }
}
