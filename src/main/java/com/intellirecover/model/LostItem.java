package com.intellirecover.model;

import java.time.LocalDate;

/**
 * Represents a lost item reported by a student.
 * Demonstrates inheritance and method overriding.
 */
public class LostItem extends Item {
    private LocalDate dateLost;
    private String rewardNotes;

    public LostItem(String id, String name, ItemCategory category, String description,
                    String location, LocalDate reportedDate, ItemStatus status,
                    String reporterUserId, String fingerprint, String brand,
                    String distinctiveAttributes, LocalDate dateLost, String rewardNotes) {
        super(id, name, category, description, location, reportedDate, status,
                reporterUserId, fingerprint, brand, distinctiveAttributes);
        this.dateLost = dateLost != null ? dateLost : (reportedDate != null ? reportedDate : LocalDate.now());
        this.rewardNotes = rewardNotes != null ? rewardNotes.trim() : "";
    }

    public LocalDate getDateLost() {
        return dateLost;
    }

    public void setDateLost(LocalDate dateLost) {
        this.dateLost = dateLost;
    }

    public String getRewardNotes() {
        return rewardNotes;
    }

    public void setRewardNotes(String rewardNotes) {
        this.rewardNotes = rewardNotes;
    }

    @Override
    public String getItemType() {
        return "LOST";
    }

    @Override
    public LocalDate getIncidentDate() {
        return dateLost;
    }
}
