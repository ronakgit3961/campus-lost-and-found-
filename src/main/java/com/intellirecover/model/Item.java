package com.intellirecover.model;

import java.time.LocalDate;

/**
 * Abstract base class representing a campus item report (either Lost or Found).
 * Demonstrates OOP Abstraction and Encapsulation.
 */
public abstract class Item {
    private String id;
    private String name;
    private ItemCategory category;
    private String description;
    private String location;
    private LocalDate reportedDate;
    private ItemStatus status;
    private String reporterUserId;
    private String fingerprint;
    private String brand;
    private String distinctiveAttributes;

    public Item(String id, String name, ItemCategory category, String description,
                String location, LocalDate reportedDate, ItemStatus status,
                String reporterUserId, String fingerprint, String brand,
                String distinctiveAttributes) {
        this.id = id;
        this.name = name;
        this.category = category != null ? category : ItemCategory.OTHER;
        this.description = description;
        this.location = location;
        this.reportedDate = reportedDate != null ? reportedDate : LocalDate.now();
        this.status = status != null ? status : ItemStatus.REPORTED;
        this.reporterUserId = reporterUserId;
        this.fingerprint = fingerprint;
        this.brand = brand != null ? brand.trim() : "";
        this.distinctiveAttributes = distinctiveAttributes != null ? distinctiveAttributes.trim() : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDate reportedDate) {
        this.reportedDate = reportedDate;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(String reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDistinctiveAttributes() {
        return distinctiveAttributes;
    }

    public void setDistinctiveAttributes(String distinctiveAttributes) {
        this.distinctiveAttributes = distinctiveAttributes;
    }

    /**
     * Polymorphic discriminator method.
     */
    public abstract String getItemType();

    /**
     * Polymorphic item date accessor (lost date vs found date).
     */
    public abstract LocalDate getIncidentDate();

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | Status: %s",
                id, name, category.getDisplayName(), location, status.getDescription());
    }
}
