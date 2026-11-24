package com.intellirecover.service;

import com.intellirecover.model.FoundItem;
import com.intellirecover.model.ItemStatus;
import com.intellirecover.repository.ItemRepository;
import com.intellirecover.util.DateUtil;

import java.time.LocalDate;
import java.util.*;

/**
 * FEATURE 7: Smart Expiry and Rescue.
 * Identifies long-unclaimed found items using PriorityQueue ranking,
 * calculating days overdue and suggesting ethical administrative actions.
 */
public class RescueService {
    private final ItemRepository itemRepository;
    private int holdingThresholdDays = 30; // Configurable holding period

    public static class RescueCandidate implements Comparable<RescueCandidate> {
        private final FoundItem item;
        private final long daysElapsed;
        private final long overdueDays;
        private final String recommendedAction;

        public RescueCandidate(FoundItem item, long daysElapsed, long overdueDays, String recommendedAction) {
            this.item = item;
            this.daysElapsed = daysElapsed;
            this.overdueDays = overdueDays;
            this.recommendedAction = recommendedAction;
        }

        public FoundItem getItem() {
            return item;
        }

        public long getDaysElapsed() {
            return daysElapsed;
        }

        public long getOverdueDays() {
            return overdueDays;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        @Override
        public int compareTo(RescueCandidate o) {
            // PriorityQueue ordering: highest overdue days first
            return Long.compare(o.daysElapsed, this.daysElapsed);
        }
    }

    public RescueService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public RescueService(ItemRepository itemRepository, int holdingThresholdDays) {
        this.itemRepository = itemRepository;
        this.holdingThresholdDays = holdingThresholdDays;
    }

    public int getHoldingThresholdDays() {
        return holdingThresholdDays;
    }

    public void setHoldingThresholdDays(int holdingThresholdDays) {
        this.holdingThresholdDays = holdingThresholdDays;
    }

    /**
     * Scans active found items and returns a prioritized list of unclaimed candidates.
     */
    public List<RescueCandidate> getUnclaimedPriorityList() {
        PriorityQueue<RescueCandidate> queue = new PriorityQueue<>();
        LocalDate today = LocalDate.now();

        for (FoundItem item : itemRepository.getAllFoundItems()) {
            if (item.getStatus() == ItemStatus.REPORTED ||
                item.getStatus() == ItemStatus.MATCH_PENDING ||
                item.getStatus() == ItemStatus.EXPIRED) {

                long days = DateUtil.daysBetween(item.getDateFound(), today);
                long overdue = days - holdingThresholdDays;

                String action = determineAction(item, days);
                queue.add(new RescueCandidate(item, days, overdue, action));
            }
        }

        List<RescueCandidate> list = new ArrayList<>();
        while (!queue.isEmpty()) {
            list.add(queue.poll());
        }
        return list;
    }

    private String determineAction(FoundItem item, long days) {
        if (days > 60) {
            return "Eligible for University Charity Donation or Department Reallocation";
        } else if (days > 45) {
            return "Issue Secondary Public Bulletin Notice & Escalate to Campus Security";
        } else if (days >= holdingThresholdDays) {
            return "Transfer to Central Long-Term Security Storage Vault";
        } else {
            long remaining = holdingThresholdDays - days;
            return String.format("Standard Holding Window: %d days remaining", remaining);
        }
    }
}
