package com.intellirecover.repository;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.Claim;
import com.intellirecover.model.ClaimStatus;
import com.intellirecover.storage.FileStorageManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for item ownership claims backed by CSV storage.
 * Enforces claim uniqueness and prevents simultaneous competing claims.
 */
public class ClaimRepository {
    private final Map<String, Claim> claimsById = new HashMap<>();
    private final FileStorageManager storageManager;

    public ClaimRepository(FileStorageManager storageManager) {
        this.storageManager = storageManager;
        load();
    }

    public synchronized void load() {
        try {
            claimsById.clear();
            for (Claim c : storageManager.loadClaims()) {
                claimsById.put(c.getId(), c);
            }
        } catch (StorageException e) {
            System.err.println("Error loading claims: " + e.getMessage());
        }
    }

    public synchronized void save(Claim claim) throws StorageException {
        claimsById.put(claim.getId(), claim);
        storageManager.saveClaims(claimsById.values());
    }

    public synchronized Optional<Claim> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(claimsById.get(id));
    }

    public synchronized List<Claim> findByFoundItemId(String foundItemId) {
        if (foundItemId == null) return Collections.emptyList();
        return claimsById.values().stream()
                .filter(c -> foundItemId.equalsIgnoreCase(c.getFoundItemId()))
                .collect(Collectors.toList());
    }

    public synchronized List<Claim> findByClaimantId(String claimantUserId) {
        if (claimantUserId == null) return Collections.emptyList();
        return claimsById.values().stream()
                .filter(c -> claimantUserId.equalsIgnoreCase(c.getClaimantUserId()))
                .collect(Collectors.toList());
    }

    public synchronized List<Claim> findAll() {
        return new ArrayList<>(claimsById.values());
    }

    /**
     * FEATURE 5: Claim Protection.
     * Returns true if there is an active claim (PENDING or APPROVED) for this item,
     * preventing multiple claimants or conflicting claim states.
     */
    public synchronized boolean hasActiveClaim(String foundItemId) {
        if (foundItemId == null) return false;
        return claimsById.values().stream()
                .anyMatch(c -> foundItemId.equalsIgnoreCase(c.getFoundItemId()) &&
                        (c.getStatus() == ClaimStatus.PENDING || c.getStatus() == ClaimStatus.APPROVED));
    }
}
