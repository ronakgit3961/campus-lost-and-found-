package com.intellirecover.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item found on campus and turned in or reported.
 * Stores private verification questions securely to protect true ownership.
 */
public class FoundItem extends Item {
    private LocalDate dateFound;
    private String storageLocation;
    private List<VerificationQuestion> secretQuestions;
    private String currentClaimId;

    public FoundItem(String id, String name, ItemCategory category, String description,
                     String location, LocalDate reportedDate, ItemStatus status,
                     String reporterUserId, String fingerprint, String brand,
                     String distinctiveAttributes, LocalDate dateFound,
                     String storageLocation, List<VerificationQuestion> secretQuestions,
                     String currentClaimId) {
        super(id, name, category, description, location, reportedDate, status,
                reporterUserId, fingerprint, brand, distinctiveAttributes);
        this.dateFound = dateFound != null ? dateFound : (reportedDate != null ? reportedDate : LocalDate.now());
        this.storageLocation = storageLocation != null ? storageLocation.trim() : "Campus Security Desk";
        this.secretQuestions = secretQuestions != null ? secretQuestions : new ArrayList<>();
        this.currentClaimId = currentClaimId;
    }

    public LocalDate getDateFound() {
        return dateFound;
    }

    public void setDateFound(LocalDate dateFound) {
        this.dateFound = dateFound;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public List<VerificationQuestion> getSecretQuestions() {
        return secretQuestions;
    }

    public void setSecretQuestions(List<VerificationQuestion> secretQuestions) {
        this.secretQuestions = secretQuestions != null ? secretQuestions : new ArrayList<>();
    }

    public void addSecretQuestion(VerificationQuestion question) {
        if (question != null) {
            this.secretQuestions.add(question);
        }
    }

    public boolean hasSecretQuestions() {
        return secretQuestions != null && !secretQuestions.isEmpty();
    }

    public String getCurrentClaimId() {
        return currentClaimId;
    }

    public void setCurrentClaimId(String currentClaimId) {
        this.currentClaimId = currentClaimId;
    }

    @Override
    public String getItemType() {
        return "FOUND";
    }

    @Override
    public LocalDate getIncidentDate() {
        return dateFound;
    }
}
