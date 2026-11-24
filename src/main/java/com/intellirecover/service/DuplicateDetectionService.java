package com.intellirecover.service;

import com.intellirecover.model.Item;
import com.intellirecover.util.DateUtil;

import java.util.*;

/**
 * FEATURE 4: Duplicate Report Detection.
 * Scans active reports of the same type to detect accidental or duplicate filings
 * before saving, warning the student with actionable options.
 */
public class DuplicateDetectionService {
    private final MatchingService matchingService;
    private double duplicateThreshold = 70.0;

    public static class DuplicateWarning {
        private final Item existingItem;
        private final double similarityPercentage;
        private final boolean fingerprintMatch;
        private final String explanation;

        public DuplicateWarning(Item existingItem, double similarityPercentage,
                                boolean fingerprintMatch, String explanation) {
            this.existingItem = existingItem;
            this.similarityPercentage = similarityPercentage;
            this.fingerprintMatch = fingerprintMatch;
            this.explanation = explanation;
        }

        public Item getExistingItem() {
            return existingItem;
        }

        public double getSimilarityPercentage() {
            return similarityPercentage;
        }

        public boolean isFingerprintMatch() {
            return fingerprintMatch;
        }

        public String getExplanation() {
            return explanation;
        }
    }

    public DuplicateDetectionService(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    public double getDuplicateThreshold() {
        return duplicateThreshold;
    }

    public void setDuplicateThreshold(double duplicateThreshold) {
        this.duplicateThreshold = duplicateThreshold;
    }

    /**
     * Checks a candidate item against a collection of existing items of the same type.
     * Returns an Optional DuplicateWarning if high similarity is discovered.
     */
    public Optional<DuplicateWarning> checkDuplicate(Item candidate, Collection<? extends Item> existingItems) {
        if (candidate == null || existingItems == null || existingItems.isEmpty()) {
            return Optional.empty();
        }

        DuplicateWarning highestWarning = null;
        double maxSimilarity = 0.0;

        for (Item existing : existingItems) {
            // Ignore comparing an item against itself if already saved
            if (candidate.getId() != null && candidate.getId().equals(existing.getId())) {
                continue;
            }

            // Fingerprint exact match
            boolean fpMatch = candidate.getFingerprint() != null &&
                              candidate.getFingerprint().equalsIgnoreCase(existing.getFingerprint());

            double similarity = calculateSimilarity(candidate, existing);

            if (fpMatch) {
                similarity = Math.max(similarity, 95.0);
            }

            if (similarity >= duplicateThreshold && similarity > maxSimilarity) {
                maxSimilarity = similarity;
                String explanation = fpMatch
                        ? "Item DNA Fingerprint matches existing report exactly."
                        : String.format("High similarity in description, category (%s), and location (%s).",
                                existing.getCategory().getDisplayName(), existing.getLocation());

                highestWarning = new DuplicateWarning(existing, similarity, fpMatch, explanation);
            }
        }

        return Optional.ofNullable(highestWarning);
    }

    private double calculateSimilarity(Item a, Item b) {
        double score = 0.0;

        // 1. Category (30%)
        if (a.getCategory() == b.getCategory()) {
            score += 30.0;
        }

        // 2. Keyword Jaccard (40%)
        Set<String> kwA = matchingService.extractKeywords(a.getName() + " " + a.getDescription());
        Set<String> kwB = matchingService.extractKeywords(b.getName() + " " + b.getDescription());
        double jaccard = matchingService.calculateJaccardSimilarity(kwA, kwB);
        score += jaccard * 40.0;

        // 3. Location (15%)
        if (a.getLocation() != null && b.getLocation() != null) {
            if (a.getLocation().trim().equalsIgnoreCase(b.getLocation().trim())) {
                score += 15.0;
            } else {
                Set<String> locKwA = matchingService.extractKeywords(a.getLocation());
                Set<String> locKwB = matchingService.extractKeywords(b.getLocation());
                if (!locKwA.isEmpty() && !locKwB.isEmpty() && matchingService.calculateJaccardSimilarity(locKwA, locKwB) > 0.3) {
                    score += 10.0;
                }
            }
        }

        // 4. Date Proximity (15%)
        if (a.getIncidentDate() != null && b.getIncidentDate() != null) {
            long days = DateUtil.daysBetween(a.getIncidentDate(), b.getIncidentDate());
            if (days == 0) score += 15.0;
            else if (days <= 2) score += 10.0;
            else if (days <= 5) score += 5.0;
        }

        return Math.min(100.0, score);
    }
}
