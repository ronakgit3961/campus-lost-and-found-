package com.intellirecover.model;

/**
 * Classification tiers for automated match recommendations.
 */
public enum MatchTier {
    STRONG_MATCH("STRONG MATCH", 80.0, 100.0),
    POSSIBLE_MATCH("POSSIBLE MATCH", 60.0, 79.99),
    WEAK_MATCH("WEAK MATCH", 40.0, 59.99),
    NO_MATCH("NO MATCH", 0.0, 39.99);

    private final String label;
    private final double minScore;
    private final double maxScore;

    MatchTier(String label, double minScore, double maxScore) {
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getLabel() {
        return label;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public static MatchTier fromScore(double score) {
        if (score >= 80.0) {
            return STRONG_MATCH;
        } else if (score >= 60.0) {
            return POSSIBLE_MATCH;
        } else if (score >= 40.0) {
            return WEAK_MATCH;
        } else {
            return NO_MATCH;
        }
    }
}
