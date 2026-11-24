package com.intellirecover.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the deterministic matching computation between a LostItem and a FoundItem.
 * Contains the comprehensive breakdown of points and reasons for full transparency.
 */
public class MatchResult implements Comparable<MatchResult> {
    private LostItem lostItem;
    private FoundItem foundItem;
    private double totalScore;
    private double categoryScore;
    private double keywordScore;
    private double locationScore;
    private double dateScore;
    private double attributeScore;
    private List<String> reasons;
    private MatchTier tier;

    public MatchResult(LostItem lostItem, FoundItem foundItem, double totalScore,
                       double categoryScore, double keywordScore, double locationScore,
                       double dateScore, double attributeScore, List<String> reasons) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.totalScore = Math.min(100.0, Math.max(0.0, totalScore));
        this.categoryScore = categoryScore;
        this.keywordScore = keywordScore;
        this.locationScore = locationScore;
        this.dateScore = dateScore;
        this.attributeScore = attributeScore;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.tier = MatchTier.fromScore(this.totalScore);
    }

    public LostItem getLostItem() {
        return lostItem;
    }

    public FoundItem getFoundItem() {
        return foundItem;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getCategoryScore() {
        return categoryScore;
    }

    public double getKeywordScore() {
        return keywordScore;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public double getDateScore() {
        return dateScore;
    }

    public double getAttributeScore() {
        return attributeScore;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public MatchTier getTier() {
        return tier;
    }

    /**
     * Natural ordering for PriorityQueue and sorting: highest score first.
     */
    @Override
    public int compareTo(MatchResult other) {
        return Double.compare(other.totalScore, this.totalScore);
    }

    public String getFormattedExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append("LOST ITEM: ").append(lostItem.getName()).append(" (ID: ").append(lostItem.getId()).append(")\n");
        sb.append("POSSIBLE FOUND ITEM: ").append(foundItem.getName()).append(" (ID: ").append(foundItem.getId()).append(")\n");
        sb.append(String.format("Match Score: %.0f/100\n\n", totalScore));
        sb.append("Reasons:\n");
        for (String r : reasons) {
            sb.append(" - ").append(r).append("\n");
        }
        sb.append("\nClassification: ").append(tier.getLabel());
        return sb.toString();
    }
}
