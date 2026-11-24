package com.intellirecover.model;

import java.time.LocalDateTime;

/**
 * Abstract base class representing any authenticated user in the IntelliRecover system.
 * Demonstrates OOP Encapsulation and Inheritance principles.
 */
public abstract class User {
    private String id;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;

    public User(String id, String username, String passwordHash, String salt,
                String fullName, String email, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Polymorphic method overridden by subclasses to display role-specific headings.
     */
    public abstract String getRoleHeader();

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, %s)", id, fullName, username, role);
    }
}
