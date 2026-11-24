package com.intellirecover.model;

/**
 * Lifecycle status of an item report in IntelliRecover.
 */
public enum ItemStatus {
    REPORTED("Reported & Active"),
    MATCH_PENDING("Potential Match Found"),
    CLAIM_PENDING("Claim Under Review"),
    VERIFIED("Ownership Verified"),
    RETURNED("Returned to Owner"),
    EXPIRED("Unclaimed & Expired"),
    ARCHIVED("Archived / Closed");

    private final String description;

    ItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ItemStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return REPORTED;
        }
        for (ItemStatus status : ItemStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim()) ||
                status.description.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return REPORTED;
    }
}
