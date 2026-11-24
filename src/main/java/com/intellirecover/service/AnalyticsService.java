package com.intellirecover.service;

import com.intellirecover.model.*;
import com.intellirecover.repository.ClaimRepository;
import com.intellirecover.repository.ItemRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FEATURE 6: Campus Lost Item Heatmap & Analytics.
 * Generates terminal-based text analytics and ASCII bar distributions
 * across campus locations, item categories, and recovery metrics.
 */
public class AnalyticsService {
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;

    public AnalyticsService(ItemRepository itemRepository, ClaimRepository claimRepository) {
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
    }

    public String generateHeatmapDashboard() {
        List<LostItem> lostItems = itemRepository.getAllLostItems();
        List<FoundItem> foundItems = itemRepository.getAllFoundItems();
        List<Claim> claims = claimRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("            CAMPUS INTELLIRECOVER - RECOVERY ANALYTICS & HEATMAP       \n");
        sb.append("======================================================================\n\n");

        // 1. Core Summary Metrics
        long totalLost = lostItems.size();
        long totalFound = foundItems.size();
        long returnedFound = foundItems.stream().filter(f -> f.getStatus() == ItemStatus.RETURNED).count();
        long pendingClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.PENDING).count();
        long unclaimedFound = foundItems.stream()
                .filter(f -> f.getStatus() == ItemStatus.REPORTED || f.getStatus() == ItemStatus.MATCH_PENDING)
                .count();

        double recoveryRate = (totalFound > 0) ? ((double) returnedFound / totalFound) * 100.0 : 0.0;

        sb.append("KEY METRICS SUMMARY:\n");
        sb.append(String.format(" - Total Lost Items Reported  : %d\n", totalLost));
        sb.append(String.format(" - Total Found Items Handed In: %d\n", totalFound));
        sb.append(String.format(" - Successfully Recovered     : %d (%.1f%% overall recovery rate)\n", returnedFound, recoveryRate));
        sb.append(String.format(" - Pending Ownership Claims   : %d\n", pendingClaims));
        sb.append(String.format(" - Active Unclaimed Items     : %d\n\n", unclaimedFound));

        // 2. Campus Hotspots (Combined Locations)
        sb.append("----------------------------------------------------------------------\n");
        sb.append("CAMPUS INCIDENT HOTSPOTS (INCIDENTS BY LOCATION):\n");
        sb.append("----------------------------------------------------------------------\n");
        Map<String, Integer> locationCounts = new HashMap<>();
        for (LostItem l : lostItems) {
            String norm = normalizeLocation(l.getLocation());
            locationCounts.put(norm, locationCounts.getOrDefault(norm, 0) + 1);
        }
        for (FoundItem f : foundItems) {
            String norm = normalizeLocation(f.getLocation());
            locationCounts.put(norm, locationCounts.getOrDefault(norm, 0) + 1);
        }
        sb.append(renderBarChart(locationCounts, 25));

        // 3. Category Distribution
        sb.append("\n----------------------------------------------------------------------\n");
        sb.append("MOST FREQUENTLY LOST CATEGORIES:\n");
        sb.append("----------------------------------------------------------------------\n");
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (LostItem l : lostItems) {
            String catName = l.getCategory().getDisplayName();
            categoryCounts.put(catName, categoryCounts.getOrDefault(catName, 0) + 1);
        }
        sb.append(renderBarChart(categoryCounts, 25));

        // 4. Recovered Category Success
        sb.append("\n----------------------------------------------------------------------\n");
        sb.append("SUCCESSFULLY RECOVERED ITEMS BY CATEGORY:\n");
        sb.append("----------------------------------------------------------------------\n");
        Map<String, Integer> recoveredCatCounts = new HashMap<>();
        for (FoundItem f : foundItems) {
            if (f.getStatus() == ItemStatus.RETURNED) {
                String catName = f.getCategory().getDisplayName();
                recoveredCatCounts.put(catName, recoveredCatCounts.getOrDefault(catName, 0) + 1);
            }
        }
        if (recoveredCatCounts.isEmpty()) {
            sb.append(" No items officially returned yet.\n");
        } else {
            sb.append(renderBarChart(recoveredCatCounts, 25));
        }

        sb.append("======================================================================\n");
        return sb.toString();
    }

    private String normalizeLocation(String loc) {
        if (loc == null || loc.trim().isEmpty()) return "UNKNOWN";
        String lower = loc.toLowerCase().trim();
        if (lower.contains("library")) return "CENTRAL LIBRARY";
        if (lower.contains("cafeteria") || lower.contains("canteen") || lower.contains("cafe")) return "CAMPUS CAFETERIA";
        if (lower.contains("sports") || lower.contains("ground") || lower.contains("gym")) return "SPORTS COMPLEX";
        if (lower.contains("lab") || lower.contains("laboratory")) return "SCIENCE & ENG LABS";
        if (lower.contains("hostel") || lower.contains("dorm")) return "STUDENT HOSTELS";
        if (lower.contains("parking")) return "PARKING LOTS";
        if (lower.contains("auditorium")) return "MAIN AUDITORIUM";
        return loc.trim().toUpperCase();
    }

    private String renderBarChart(Map<String, Integer> data, int maxBarWidth) {
        if (data == null || data.isEmpty()) {
            return " No data records available.\n";
        }

        // Sort descending by count using TreeMap / List sorting
        List<Map.Entry<String, Integer>> sorted = data.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(8)
                .collect(Collectors.toList());

        int maxVal = sorted.get(0).getValue();
        if (maxVal == 0) maxVal = 1;

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : sorted) {
            String label = entry.getKey();
            if (label.length() > 20) {
                label = label.substring(0, 17) + "...";
            }
            int val = entry.getValue();
            int barLength = Math.max(1, (int) Math.round(((double) val / maxVal) * maxBarWidth));

            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLength; i++) {
                bar.append('#');
            }

            sb.append(String.format("%-22s | %-26s %d\n", label, bar.toString(), val));
        }
        return sb.toString();
    }
}
