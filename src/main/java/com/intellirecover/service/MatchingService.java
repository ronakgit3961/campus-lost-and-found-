package com.intellirecover.service;

import com.intellirecover.model.FoundItem;
import com.intellirecover.model.LostItem;
import com.intellirecover.model.MatchResult;
import com.intellirecover.model.MatchTier;
import com.intellirecover.util.DateUtil;

import java.util.*;

/**
 * FEATURE 1: Intelligent Match Engine.
 * Implements a deterministic Java matching algorithm with transparent weighted scoring,
 * Jaccard text similarity, and PriorityQueue ranking.
 */
public class MatchingService {
    // Configurable default thresholds
    private double minDisplayScore = 40.0;

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "a", "an", "is", "in", "at", "of", "on", "for", "with",
            "my", "to", "and", "or", "it", "this", "that", "was", "near",
            "found", "lost", "by", "from", "inside", "outside", "room", "floor"
    ));

    public MatchingService() {}

    public MatchingService(double minDisplayScore) {
        this.minDisplayScore = minDisplayScore;
    }

    public double getMinDisplayScore() {
        return minDisplayScore;
    }

    public void setMinDisplayScore(double minDisplayScore) {
        this.minDisplayScore = minDisplayScore;
    }

    /**
     * Evaluates a lost item against all candidate found items and returns
     * matches ranked highest-to-lowest using a PriorityQueue.
     */
    public List<MatchResult> findMatchesForLostItem(LostItem lostItem, List<FoundItem> candidateFoundItems) {
        PriorityQueue<MatchResult> queue = new PriorityQueue<>();

        for (FoundItem foundItem : candidateFoundItems) {
            MatchResult result = calculateMatch(lostItem, foundItem);
            if (result.getTotalScore() >= minDisplayScore) {
                queue.add(result);
            }
        }

        List<MatchResult> rankedResults = new ArrayList<>();
        while (!queue.isEmpty()) {
            rankedResults.add(queue.poll());
        }
        return rankedResults;
    }

    /**
     * Computes the weighted 100-point similarity score between a lost and found item.
     */
    public MatchResult calculateMatch(LostItem lostItem, FoundItem foundItem) {
        List<String> reasons = new ArrayList<>();

        // 1. Category Similarity (Max 20 Points)
        double categoryScore = 0.0;
        if (lostItem.getCategory() == foundItem.getCategory()) {
            categoryScore = 20.0;
            reasons.add(String.format("Same category (%s): +20 pts", lostItem.getCategory().getDisplayName()));
        } else {
            reasons.add(String.format("Different categories (%s vs %s): +0 pts",
                    lostItem.getCategory().getDisplayName(), foundItem.getCategory().getDisplayName()));
        }

        // 2. Keyword Jaccard Similarity (Max 35 Points)
        Set<String> lostKeywords = extractKeywords(lostItem.getName() + " " + lostItem.getDescription());
        Set<String> foundKeywords = extractKeywords(foundItem.getName() + " " + foundItem.getDescription());

        double jaccard = calculateJaccardSimilarity(lostKeywords, foundKeywords);
        double keywordScore = Math.round(jaccard * 35.0 * 10.0) / 10.0;

        Set<String> commonKeywords = new HashSet<>(lostKeywords);
        commonKeywords.retainAll(foundKeywords);

        if (keywordScore > 0) {
            reasons.add(String.format("Description keyword similarity (Jaccard: %.2f, shared: %s): +%.1f pts",
                    jaccard, commonKeywords, keywordScore));
        } else {
            reasons.add("No overlapping descriptive keywords: +0 pts");
        }

        // 3. Location Similarity (Max 20 Points)
        double locationScore = calculateLocationScore(lostItem.getLocation(), foundItem.getLocation(), reasons);

        // 4. Date Proximity (Max 15 Points)
        double dateScore = calculateDateScore(lostItem.getDateLost(), foundItem.getDateFound(), reasons);

        // 5. Brand & Distinctive Attributes (Max 10 Points)
        double attributeScore = calculateAttributeScore(lostItem, foundItem, reasons);

        double totalScore = Math.min(100.0, categoryScore + keywordScore + locationScore + dateScore + attributeScore);

        return new MatchResult(lostItem, foundItem, totalScore, categoryScore,
                keywordScore, locationScore, dateScore, attributeScore, reasons);
    }

    /**
     * Extracts cleaned unique keywords, removing punctuation and stop-words.
     */
    public Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return keywords;
        }

        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = cleaned.split("\\s+");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.length() > 1 && !STOP_WORDS.contains(trimmed)) {
                keywords.add(trimmed);
            }
        }
        return keywords;
    }

    /**
     * Computes Jaccard Similarity = |A ∩ B| / |A ∪ B|.
     */
    public double calculateJaccardSimilarity(Set<String> setA, Set<String> setB) {
        if (setA == null || setB == null || setA.isEmpty() || setB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    private double calculateLocationScore(String loc1, String loc2, List<String> reasons) {
        if (loc1 == null || loc2 == null || loc1.trim().isEmpty() || loc2.trim().isEmpty()) {
            reasons.add("Missing location information: +0 pts");
            return 0.0;
        }

        String l1 = loc1.trim().toLowerCase();
        String l2 = loc2.trim().toLowerCase();

        if (l1.equals(l2)) {
            reasons.add(String.format("Exact campus location match (\"%s\"): +20 pts", loc1));
            return 20.0;
        }

        // Check common building / zone keywords
        Set<String> locTokens1 = extractKeywords(l1);
        Set<String> locTokens2 = extractKeywords(l2);
        Set<String> sharedLoc = new HashSet<>(locTokens1);
        sharedLoc.retainAll(locTokens2);

        if (!sharedLoc.isEmpty()) {
            reasons.add(String.format("Same campus facility or building area (%s): +14 pts", sharedLoc));
            return 14.0;
        }

        reasons.add(String.format("Different campus locations (\"%s\" vs \"%s\"): +0 pts", loc1, loc2));
        return 0.0;
    }

    private double calculateDateScore(java.time.LocalDate d1, java.time.LocalDate d2, List<String> reasons) {
        if (d1 == null || d2 == null) {
            reasons.add("Date of incident unavailable: +0 pts");
            return 0.0;
        }

        long diff = DateUtil.daysBetween(d1, d2);
        if (diff == 0) {
            reasons.add("Exact same date of occurrence: +15 pts");
            return 15.0;
        } else if (diff <= 1) {
            reasons.add(String.format("Incident dates within %d day: +13 pts", diff));
            return 13.0;
        } else if (diff <= 3) {
            reasons.add(String.format("Incident dates within %d days: +10 pts", diff));
            return 10.0;
        } else if (diff <= 7) {
            reasons.add(String.format("Incident dates within %d days: +6 pts", diff));
            return 6.0;
        } else if (diff <= 14) {
            reasons.add(String.format("Incident dates within %d days: +3 pts", diff));
            return 3.0;
        } else {
            reasons.add(String.format("Incident dates separated by %d days: +0 pts", diff));
            return 0.0;
        }
    }

    private double calculateAttributeScore(LostItem lost, FoundItem found, List<String> reasons) {
        double score = 0.0;
        boolean brandMatched = false;

        // Check brand
        String b1 = lost.getBrand();
        String b2 = found.getBrand();
        if (b1 != null && b2 != null && !b1.trim().isEmpty() && !b2.trim().isEmpty()) {
            if (b1.trim().equalsIgnoreCase(b2.trim())) {
                score += 6.0;
                brandMatched = true;
                reasons.add(String.format("Matching brand (\"%s\"): +6 pts", b1));
            }
        }

        // Check distinctive attributes
        String attr1 = lost.getDistinctiveAttributes();
        String attr2 = found.getDistinctiveAttributes();
        if (attr1 != null && attr2 != null && !attr1.trim().isEmpty() && !attr2.trim().isEmpty()) {
            Set<String> kw1 = extractKeywords(attr1);
            Set<String> kw2 = extractKeywords(attr2);
            Set<String> shared = new HashSet<>(kw1);
            shared.retainAll(kw2);

            if (!shared.isEmpty()) {
                double attrPts = brandMatched ? 4.0 : 7.0;
                score += attrPts;
                reasons.add(String.format("Similar identifying attributes (%s): +%.0f pts", shared, attrPts));
            }
        }

        if (score == 0.0) {
            reasons.add("No matching brand or distinctive attributes recorded: +0 pts");
        }

        return Math.min(10.0, score);
    }
}
