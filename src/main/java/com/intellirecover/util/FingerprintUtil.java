package com.intellirecover.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Utility for generating the Item DNA Fingerprint using SHA-256 over normalized attributes.
 * Distinguishes items and assists in deterministic duplicate detection.
 */
public final class FingerprintUtil {

    private FingerprintUtil() {
        // Prevent instantiation
    }

    /**
     * Generates a 64-character SHA-256 hex string based on normalized item properties.
     */
    public static String generateFingerprint(String category, String name, String brand,
                                             Set<String> keywords, String distinctiveAttributes) {
        StringBuilder raw = new StringBuilder();

        // 1. Normalized Category
        raw.append(category != null ? category.trim().toUpperCase() : "GENERAL").append("|");

        // 2. Normalized Item Name (lowercased alphanumeric tokens)
        raw.append(name != null ? name.trim().toLowerCase().replaceAll("[^a-z0-9]", "") : "").append("|");

        // 3. Normalized Brand
        raw.append(brand != null ? brand.trim().toLowerCase().replaceAll("[^a-z0-9]", "") : "GENERIC").append("|");

        // 4. Sorted Normalized Keywords for deterministic ordering
        if (keywords != null && !keywords.isEmpty()) {
            List<String> sortedKw = new ArrayList<>(keywords);
            Collections.sort(sortedKw);
            raw.append(String.join(",", sortedKw));
        }
        raw.append("|");

        // 5. Distinctive Attributes
        raw.append(distinctiveAttributes != null ? distinctiveAttributes.trim().toLowerCase() : "");

        return computeSha256(raw.toString());
    }

    /**
     * Returns a shortened 12-character representation of the fingerprint for clean terminal display.
     */
    public static String getShortFingerprint(String fullFingerprint) {
        if (fullFingerprint == null || fullFingerprint.length() < 12) {
            return fullFingerprint != null ? fullFingerprint : "N/A";
        }
        return fullFingerprint.substring(0, 12).toUpperCase();
    }

    private static String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing from JVM", e);
        }
    }
}
