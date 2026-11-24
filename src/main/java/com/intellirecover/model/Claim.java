package com.intellirecover.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an ownership claim on a found item.
 * Encapsulates claimant responses, verification score, and administrative review status.
 */
public class Claim {
    private String id;
    private String foundItemId;
    private String claimantUserId;
    private LocalDateTime submissionDate;
    private List<String> submittedAnswers;
    private double verificationScore;
    private ClaimStatus status;
    private String reviewNotes;
    private String reviewedByAdminId;
    private LocalDateTime reviewedAt;

    public Claim(String id, String foundItemId, String claimantUserId,
                 LocalDateTime submissionDate, List<String> submittedAnswers,
                 double verificationScore, ClaimStatus status, String reviewNotes,
                 String reviewedByAdminId, LocalDateTime reviewedAt) {
        this.id = id;
        this.foundItemId = foundItemId;
        this.claimantUserId = claimantUserId;
        this.submissionDate = submissionDate != null ? submissionDate : LocalDateTime.now();
        this.submittedAnswers = submittedAnswers != null ? submittedAnswers : new ArrayList<>();
        this.verificationScore = verificationScore;
        this.status = status != null ? status : ClaimStatus.PENDING;
        this.reviewNotes = reviewNotes != null ? reviewNotes : "";
        this.reviewedByAdminId = reviewedByAdminId;
        this.reviewedAt = reviewedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(String foundItemId) {
        this.foundItemId = foundItemId;
    }

    public String getClaimantUserId() {
        return claimantUserId;
    }

    public void setClaimantUserId(String claimantUserId) {
        this.claimantUserId = claimantUserId;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public List<String> getSubmittedAnswers() {
        return submittedAnswers;
    }

    public void setSubmittedAnswers(List<String> submittedAnswers) {
        this.submittedAnswers = submittedAnswers != null ? submittedAnswers : new ArrayList<>();
    }

    public double getVerificationScore() {
        return verificationScore;
    }

    public void setVerificationScore(double verificationScore) {
        this.verificationScore = verificationScore;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getReviewedByAdminId() {
        return reviewedByAdminId;
    }

    public void setReviewedByAdminId(String reviewedByAdminId) {
        this.reviewedByAdminId = reviewedByAdminId;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] Claim on %s by %s | Verification Score: %.1f%% | Status: %s",
                id, foundItemId, claimantUserId, verificationScore, status.getDescription());
    }
}
