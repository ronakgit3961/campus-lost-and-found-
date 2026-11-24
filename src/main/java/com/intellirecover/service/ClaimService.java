package com.intellirecover.service;

import com.intellirecover.exception.ClaimAlreadyExistsException;
import com.intellirecover.exception.IntelliRecoverException;
import com.intellirecover.exception.ResourceNotFoundException;
import com.intellirecover.exception.StorageException;
import com.intellirecover.model.*;
import com.intellirecover.repository.ClaimRepository;
import com.intellirecover.repository.ItemRepository;
import com.intellirecover.util.IDGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FEATURE 5: Recovery Confidence and Claim Protection.
 * Coordinates claim submissions, ownership verification evaluation, administrative approvals,
 * and status transitions with comprehensive audit logging.
 */
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final VerificationService verificationService;
    private final RecoveryService recoveryService;
    private final NotificationService notificationService;

    public ClaimService(ClaimRepository claimRepository,
                        ItemRepository itemRepository,
                        VerificationService verificationService,
                        RecoveryService recoveryService,
                        NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.verificationService = verificationService;
        this.recoveryService = recoveryService;
        this.notificationService = notificationService;
    }

    /**
     * Submits an ownership claim with claimant answers to verification questions.
     * Enforces Claim Protection: rejects if item is already claimed or under review.
     */
    public synchronized Claim submitClaim(String foundItemId, String claimantUserId, List<String> answers)
            throws IntelliRecoverException {

        Optional<FoundItem> optFound = itemRepository.findFoundItemById(foundItemId);
        if (optFound.isEmpty()) {
            throw new ResourceNotFoundException("Found item with ID '" + foundItemId + "' does not exist.");
        }

        FoundItem foundItem = optFound.get();

        if (foundItem.getStatus() == ItemStatus.RETURNED) {
            throw new IntelliRecoverException("This item has already been successfully recovered and returned to its owner.");
        }

        // Claim Protection: check if another active claim is already pending or approved
        if (claimRepository.hasActiveClaim(foundItemId)) {
            throw new ClaimAlreadyExistsException(
                    "An active claim is already being processed for this item. Simultaneous duplicate claims are prevented.");
        }

        // Log verification initiation
        recoveryService.logEvent(foundItemId, null, "VERIFICATION_INITIATED", claimantUserId,
                "Claimant initiated secret ownership verification.");

        // Calculate verification score
        double score = verificationService.evaluateAnswers(foundItem, answers);
        boolean passed = verificationService.isPassing(score);

        String claimId = IDGenerator.nextClaimId();
        ClaimStatus initialStatus = passed ? ClaimStatus.PENDING : ClaimStatus.REJECTED;
        String initialNotes = passed
                ? String.format("Verification passed with %.1f%% score. Awaiting Administrator final review.", score)
                : String.format("Automated rejection: Verification score (%.1f%%) fell below required threshold.", score);

        Claim claim = new Claim(claimId, foundItemId, claimantUserId, LocalDateTime.now(),
                answers, score, initialStatus, initialNotes, null, null);

        claimRepository.save(claim);

        if (passed) {
            foundItem.setStatus(ItemStatus.CLAIM_PENDING);
            foundItem.setCurrentClaimId(claimId);
            itemRepository.saveFoundItem(foundItem);

            recoveryService.logEvent(foundItemId, claimId, "CLAIM_SUBMITTED", claimantUserId,
                    String.format("Claim submitted successfully (Verification Score: %.1f%%). Status set to CLAIM_PENDING.", score));

            notificationService.sendNotification(claimantUserId, "Claim Submitted",
                    String.format("Your claim %s for item '%s' is submitted and pending administrative review.",
                            claimId, foundItem.getName()));
        } else {
            recoveryService.logEvent(foundItemId, claimId, "CLAIM_REJECTED", claimantUserId,
                    String.format("Claim rejected due to failing verification score: %.1f%%", score));

            notificationService.sendNotification(claimantUserId, "Claim Verification Failed",
                    String.format("Your claim %s for item '%s' could not be verified with the provided details.",
                            claimId, foundItem.getName()));
        }

        return claim;
    }

    /**
     * Campus Administrator reviews and approves a pending claim.
     */
    public synchronized void approveClaim(String claimId, String adminUserId, String reviewNotes)
            throws IntelliRecoverException {

        Optional<Claim> optClaim = claimRepository.findById(claimId);
        if (optClaim.isEmpty()) {
            throw new ResourceNotFoundException("Claim with ID '" + claimId + "' does not exist.");
        }

        Claim claim = optClaim.get();
        if (claim.getStatus() == ClaimStatus.APPROVED) {
            throw new IntelliRecoverException("Claim is already approved.");
        }

        Optional<FoundItem> optFound = itemRepository.findFoundItemById(claim.getFoundItemId());
        if (optFound.isEmpty()) {
            throw new ResourceNotFoundException("Associated found item not found.");
        }
        FoundItem foundItem = optFound.get();

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedByAdminId(adminUserId);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setReviewNotes(reviewNotes != null ? reviewNotes.trim() : "Approved by administrator.");
        claimRepository.save(claim);

        foundItem.setStatus(ItemStatus.VERIFIED);
        foundItem.setCurrentClaimId(claimId);
        itemRepository.saveFoundItem(foundItem);

        recoveryService.logEvent(foundItem.getId(), claimId, "CLAIM_APPROVED", adminUserId,
                "Claim officially approved by administrator: " + claim.getReviewNotes());

        notificationService.sendNotification(claim.getClaimantUserId(), "Claim Approved!",
                String.format("Your claim %s for '%s' has been APPROVED! Please visit %s for handover.",
                        claimId, foundItem.getName(), foundItem.getStorageLocation()));
    }

    /**
     * Campus Administrator rejects a claim. Reopens item for other potential claimants.
     */
    public synchronized void rejectClaim(String claimId, String adminUserId, String reviewNotes)
            throws IntelliRecoverException {

        Optional<Claim> optClaim = claimRepository.findById(claimId);
        if (optClaim.isEmpty()) {
            throw new ResourceNotFoundException("Claim with ID '" + claimId + "' does not exist.");
        }

        Claim claim = optClaim.get();
        Optional<FoundItem> optFound = itemRepository.findFoundItemById(claim.getFoundItemId());
        if (optFound.isEmpty()) {
            throw new ResourceNotFoundException("Associated found item not found.");
        }
        FoundItem foundItem = optFound.get();

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setReviewedByAdminId(adminUserId);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setReviewNotes(reviewNotes != null ? reviewNotes.trim() : "Rejected by administrator.");
        claimRepository.save(claim);

        // Re-open found item status for other searchers
        foundItem.setStatus(ItemStatus.REPORTED);
        foundItem.setCurrentClaimId(null);
        itemRepository.saveFoundItem(foundItem);

        recoveryService.logEvent(foundItem.getId(), claimId, "CLAIM_REJECTED", adminUserId,
                "Claim rejected by administrator: " + claim.getReviewNotes());

        notificationService.sendNotification(claim.getClaimantUserId(), "Claim Not Approved",
                String.format("Your claim %s for '%s' was not approved. Note: %s",
                        claimId, foundItem.getName(), claim.getReviewNotes()));
    }

    /**
     * Campus Administrator reopens a previously rejected claim for re-evaluation.
     */
    public synchronized void reopenClaim(String claimId, String adminUserId, String reason)
            throws IntelliRecoverException {

        Optional<Claim> optClaim = claimRepository.findById(claimId);
        if (optClaim.isEmpty()) {
            throw new ResourceNotFoundException("Claim with ID '" + claimId + "' does not exist.");
        }

        Claim claim = optClaim.get();
        Optional<FoundItem> optFound = itemRepository.findFoundItemById(claim.getFoundItemId());
        if (optFound.isEmpty()) {
            throw new ResourceNotFoundException("Associated found item not found.");
        }
        FoundItem foundItem = optFound.get();

        if (claimRepository.hasActiveClaim(foundItem.getId())) {
            throw new ClaimAlreadyExistsException("Cannot reopen: another claim is currently active on this item.");
        }

        claim.setStatus(ClaimStatus.REOPENED);
        claim.setReviewedByAdminId(adminUserId);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setReviewNotes("Reopened: " + (reason != null ? reason.trim() : ""));
        claimRepository.save(claim);

        foundItem.setStatus(ItemStatus.CLAIM_PENDING);
        foundItem.setCurrentClaimId(claimId);
        itemRepository.saveFoundItem(foundItem);

        recoveryService.logEvent(foundItem.getId(), claimId, "CLAIM_REOPENED", adminUserId,
                "Claim reopened for reconsideration: " + reason);

        notificationService.sendNotification(claim.getClaimantUserId(), "Claim Reopened",
                String.format("Your claim %s for '%s' has been reopened for reconsideration.",
                        claimId, foundItem.getName()));
    }

    /**
     * Marks the item as officially returned to the verified owner.
     */
    public synchronized void completeItemReturn(String foundItemId, String adminUserId, String handoverNotes)
            throws IntelliRecoverException {

        Optional<FoundItem> optFound = itemRepository.findFoundItemById(foundItemId);
        if (optFound.isEmpty()) {
            throw new ResourceNotFoundException("Found item with ID '" + foundItemId + "' does not exist.");
        }

        FoundItem foundItem = optFound.get();
        if (foundItem.getStatus() != ItemStatus.VERIFIED && foundItem.getStatus() != ItemStatus.CLAIM_PENDING) {
            throw new IntelliRecoverException("Item cannot be marked as returned without prior verification/approval.");
        }

        foundItem.setStatus(ItemStatus.RETURNED);
        itemRepository.saveFoundItem(foundItem);

        // Check if there is an associated lost item with the same fingerprint or reporter, update it too
        for (LostItem lost : itemRepository.getAllLostItems()) {
            if (lost.getStatus() != ItemStatus.RETURNED &&
                lost.getCategory() == foundItem.getCategory() &&
                lost.getFingerprint() != null &&
                lost.getFingerprint().equalsIgnoreCase(foundItem.getFingerprint())) {
                lost.setStatus(ItemStatus.RETURNED);
                itemRepository.saveLostItem(lost);
                recoveryService.logEvent(lost.getId(), foundItem.getCurrentClaimId(), "ITEM_RETURNED", adminUserId,
                        "Associated lost item marked as returned following found item recovery.");
                notificationService.sendNotification(lost.getReporterUserId(), "Item Recovered & Returned!",
                        String.format("Your lost item '%s' has been successfully recovered and returned!", lost.getName()));
            }
        }

        recoveryService.logEvent(foundItemId, foundItem.getCurrentClaimId(), "ITEM_RETURNED", adminUserId,
                "Item physically handed over to verified claimant. " + (handoverNotes != null ? handoverNotes : ""));

        recoveryService.logEvent(foundItemId, foundItem.getCurrentClaimId(), "RECOVERY_COMPLETED", adminUserId,
                "Recovery lifecycle completed successfully.");

        if (foundItem.getCurrentClaimId() != null) {
            Optional<Claim> optClaim = claimRepository.findById(foundItem.getCurrentClaimId());
            optClaim.ifPresent(c -> notificationService.sendNotification(c.getClaimantUserId(), "Recovery Completed",
                    String.format("Thank you! Handover for '%s' is completed. Case closed.", foundItem.getName())));
        }
    }

    public List<Claim> getClaimsForUser(String userId) {
        return claimRepository.findByClaimantId(userId);
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }
}
