package com.intellirecover.model;

/**
 * Status of an ownership claim submitted by a student.
 */
public enum ClaimStatus {
    PENDING("Pending Verification Review"),
    APPROVED("Approved by Administrator"),
    REJECTED("Rejected due to Inconsistent Details"),
    REOPENED("Reopened for Re-examination");

    private final String description;

    ClaimStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ClaimStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return PENDING;
        }
        for (ClaimStatus cs : ClaimStatus.values()) {
            if (cs.name().equalsIgnoreCase(text.trim()) ||
                cs.description.equalsIgnoreCase(text.trim())) {
                return cs;
            }
        }
        return PENDING;
    }
}
