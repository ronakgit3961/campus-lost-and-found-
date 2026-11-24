package com.intellirecover.model;

/**
 * Enumeration representing user authorization levels.
 */
public enum UserRole {
    STUDENT("Student"),
    ADMINISTRATOR("Administrator"),
    GUEST("Guest");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
