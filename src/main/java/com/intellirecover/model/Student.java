package com.intellirecover.model;

import java.time.LocalDateTime;

/**
 * Represents a student user with enrollment and contact details.
 * Demonstrates inheritance and method overriding.
 */
public class Student extends User {
    private String studentId;
    private String department;
    private String phone;

    public Student(String id, String username, String passwordHash, String salt,
                   String fullName, String email, LocalDateTime createdAt,
                   String studentId, String department, String phone) {
        super(id, username, passwordHash, salt, fullName, email, UserRole.STUDENT, createdAt);
        this.studentId = studentId;
        this.department = department;
        this.phone = phone;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getRoleHeader() {
        return String.format("STUDENT PORTAL | %s (ID: %s, Dept: %s)", getFullName(), studentId, department);
    }
}
