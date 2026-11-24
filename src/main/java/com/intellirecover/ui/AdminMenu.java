package com.intellirecover.ui;

import com.intellirecover.exception.IntelliRecoverException;
import com.intellirecover.model.*;
import com.intellirecover.service.*;
import com.intellirecover.util.DateUtil;
import com.intellirecover.util.InputValidator;

import java.util.*;

/**
 * Terminal UI controller for authenticated Campus Administrator actions.
 */
public class AdminMenu {
    private final Scanner scanner;
    private final AuthenticationService authService;
    private final ItemService itemService;
    private final ClaimService claimService;
    private final AnalyticsService analyticsService;
    private final RescueService rescueService;
    private final RecoveryService recoveryService;
    private final DuplicateDetectionService duplicateDetectionService;

    public AdminMenu(Scanner scanner,
                     AuthenticationService authService,
                     ItemService itemService,
                     ClaimService claimService,
                     AnalyticsService analyticsService,
                     RescueService rescueService,
                     RecoveryService recoveryService,
                     DuplicateDetectionService duplicateDetectionService) {
        this.scanner = scanner;
        this.authService = authService;
        this.itemService = itemService;
        this.claimService = claimService;
        this.analyticsService = analyticsService;
        this.rescueService = rescueService;
        this.recoveryService = recoveryService;
        this.duplicateDetectionService = duplicateDetectionService;
    }

    public void run() {
        boolean active = true;
        while (active && authService.isLoggedIn()) {
            User current = authService.getCurrentUser();

            System.out.println("\n==================================================");
            System.out.println("           ADMINISTRATOR DASHBOARD");
            System.out.println("  " + current.getRoleHeader());
            System.out.println("==================================================");
            System.out.println(" 1. View All Item Reports");
            System.out.println(" 2. Review & Manage Ownership Claims");
            System.out.println(" 3. Complete Item Return Handover");
            System.out.println(" 4. Manage Item Status");
            System.out.println(" 5. Scan Duplicate Reports (Duplicate Engine)");
            System.out.println(" 6. Campus Analytics & Heatmap");
            System.out.println(" 7. Smart Expiry & Unclaimed Rescue Priority");
            System.out.println(" 8. View Recovery Timeline for Item");
            System.out.println(" 9. View System Audit Activity Log");
            System.out.println(" 10. Logout");
            System.out.println("==================================================");
            System.out.print("Enter your choice (1-10): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleViewAllReports();
                case "2" -> handleManageClaims();
                case "3" -> handleCompleteReturn();
                case "4" -> handleManageItemStatus();
                case "5" -> handleScanDuplicates();
                case "6" -> handleAnalyticsHeatmap();
                case "7" -> handleUnclaimedRescue();
                case "8" -> handleViewTimeline();
                case "9" -> handleViewAuditLog();
                case "10" -> {
                    authService.logout();
                    System.out.println("\n[SUCCESS] Administrator session terminated. Returning to main menu.");
                    active = false;
                }
                default -> System.out.println("\n[ERROR] Invalid option. Please select 1 through 10.");
            }
        }
    }

    // ==========================================
    // 1. VIEW ALL REPORTS
    // ==========================================
    private void handleViewAllReports() {
        List<LostItem> lost = itemService.getAllLostItems();
        List<FoundItem> found = itemService.getAllFoundItems();

        System.out.println("\n==================================================");
        System.out.printf("  CAMPUS MASTER ITEM REGISTRY (%d Lost, %d Found)\n", lost.size(), found.size());
        System.out.println("==================================================");

        System.out.println("\n--- ALL LOST ITEMS ---");
        renderLostItems(lost);

        System.out.println("\n--- ALL FOUND ITEMS ---");
        renderFoundItems(found);
    }

    // ==========================================
    // 2. REVIEW & MANAGE CLAIMS
    // ==========================================
    private void handleManageClaims() {
        List<Claim> claims = claimService.getAllClaims();

        System.out.println("\n==================================================");
        System.out.printf("  OWNERSHIP CLAIMS MANAGEMENT (%d total)\n", claims.size());
        System.out.println("==================================================");

        if (claims.isEmpty()) {
            System.out.println(" No claims submitted yet.");
            return;
        }

        System.out.printf("%-10s | %-10s | %-10s | %-10s | %-10s | %-16s\n",
                "CLAIM ID", "FOUND ID", "CLAIMANT", "SUBMITTED", "VERIF. %", "STATUS");
        System.out.println("----------------------------------------------------------------------------------");
        for (Claim c : claims) {
            System.out.printf("%-10s | %-10s | %-10s | %-10s | %-9.1f%% | %-16s\n",
                    c.getId(),
                    c.getFoundItemId(),
                    c.getClaimantUserId(),
                    DateUtil.formatDateTime(c.getSubmissionDate()).substring(0, 10),
                    c.getVerificationScore(),
                    c.getStatus().name());
            if (!c.getReviewNotes().isEmpty()) {
                System.out.printf("   └─ Notes: %s\n", c.getReviewNotes());
            }
        }

        System.out.print("\nPerform action on a Claim? (1 = Approve, 2 = Reject, 3 = Reopen, Enter = Back): ");
        String act = scanner.nextLine().trim();
        if (!"1".equals(act) && !"2".equals(act) && !"3".equals(act)) {
            return;
        }

        System.out.print("Enter Claim ID (e.g. CLM-1001): ");
        String claimId = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter review notes or handover remarks: ");
        String notes = scanner.nextLine().trim();

        String adminId = authService.getCurrentUser().getId();
        try {
            if ("1".equals(act)) {
                claimService.approveClaim(claimId, adminId, notes);
                System.out.println("[SUCCESS] Claim approved successfully! Item status transitioned to VERIFIED.");
            } else if ("2".equals(act)) {
                claimService.rejectClaim(claimId, adminId, notes);
                System.out.println("[SUCCESS] Claim rejected. Item reopened for general recovery.");
            } else {
                claimService.reopenClaim(claimId, adminId, notes);
                System.out.println("[SUCCESS] Claim reopened for reconsideration.");
            }
        } catch (IntelliRecoverException e) {
            System.out.println("[ERROR] Claim operation failed: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. COMPLETE ITEM RETURN HANDOVER
    // ==========================================
    private void handleCompleteReturn() {
        System.out.println("\n--- COMPLETE ITEM RETURN HANDOVER ---");
        System.out.print("Enter Found Item ID to mark as Physically Returned (e.g. FND-1001): ");
        String foundId = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Handover Notes (e.g. Student presented university ID card and collected at Security Desk): ");
        String notes = scanner.nextLine().trim();

        try {
            claimService.completeItemReturn(foundId, authService.getCurrentUser().getId(), notes);
            System.out.println("\n[SUCCESS] Item return finalized! Status updated to RETURNED.");
            System.out.println("Associated timeline events logged and student notified.");
        } catch (IntelliRecoverException e) {
            System.out.println("\n[ERROR] Handover failed: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. MANAGE ITEM STATUS
    // ==========================================
    private void handleManageItemStatus() {
        System.out.println("\n--- MANAGE ITEM STATUS ---");
        System.out.print("Enter Item ID (Lost or Found, e.g. LST-1001 or FND-1001): ");
        String itemId = scanner.nextLine().trim().toUpperCase();

        Optional<Item> optItem = itemService.getItemById(itemId);
        if (optItem.isEmpty()) {
            System.out.println("[ERROR] Item with ID '" + itemId + "' not found.");
            return;
        }

        Item item = optItem.get();
        System.out.printf("Current Item: %s (%s) | Status: %s\n", item.getName(), item.getId(), item.getStatus());

        System.out.println("Select New Status:");
        ItemStatus[] statuses = ItemStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf(" [%d] %s (%s)\n", i + 1, statuses[i].name(), statuses[i].getDescription());
        }
        System.out.print("Choose status (1-" + statuses.length + "): ");
        int sel = InputValidator.parsePositiveInt(scanner.nextLine().trim(), -1);
        if (sel < 1 || sel > statuses.length) {
            System.out.println("[ERROR] Invalid selection.");
            return;
        }

        ItemStatus newStatus = statuses[sel - 1];
        try {
            if (item instanceof LostItem lost) {
                itemService.updateLostItemStatus(lost, newStatus);
            } else if (item instanceof FoundItem found) {
                itemService.updateFoundItemStatus(found, newStatus);
            }
            recoveryService.logEvent(itemId, null, "STATUS_UPDATED", authService.getCurrentUser().getId(),
                    "Administrator changed item status to " + newStatus.name());
            System.out.println("\n[SUCCESS] Item status successfully updated to " + newStatus.name());
        } catch (Exception e) {
            System.out.println("\n[ERROR] Failed to update status: " + e.getMessage());
        }
    }

    // ==========================================
    // 5. SCAN DUPLICATE REPORTS
    // ==========================================
    private void handleScanDuplicates() {
        System.out.println("\n--- SCANNING REPOSITORY FOR DUPLICATE REPORTS ---");
        List<FoundItem> foundItems = itemService.getAllFoundItems();
        List<LostItem> lostItems = itemService.getAllLostItems();

        int foundDupes = 0;
        System.out.println("\nChecking Lost Reports...");
        for (int i = 0; i < lostItems.size(); i++) {
            for (int j = i + 1; j < lostItems.size(); j++) {
                LostItem a = lostItems.get(i);
                LostItem b = lostItems.get(j);
                Optional<DuplicateDetectionService.DuplicateWarning> warn =
                        duplicateDetectionService.checkDuplicate(a, Collections.singletonList(b));
                if (warn.isPresent()) {
                    foundDupes++;
                    System.out.printf(" [POTENTIAL DUPLICATE] %s <-> %s (Similarity: %.0f%%)\n",
                            a.getId(), b.getId(), warn.get().getSimilarityPercentage());
                    System.out.println("   Reason: " + warn.get().getExplanation());
                }
            }
        }

        System.out.println("\nChecking Found Reports...");
        for (int i = 0; i < foundItems.size(); i++) {
            for (int j = i + 1; j < foundItems.size(); j++) {
                FoundItem a = foundItems.get(i);
                FoundItem b = foundItems.get(j);
                Optional<DuplicateDetectionService.DuplicateWarning> warn =
                        duplicateDetectionService.checkDuplicate(a, Collections.singletonList(b));
                if (warn.isPresent()) {
                    foundDupes++;
                    System.out.printf(" [POTENTIAL DUPLICATE] %s <-> %s (Similarity: %.0f%%)\n",
                            a.getId(), b.getId(), warn.get().getSimilarityPercentage());
                    System.out.println("   Reason: " + warn.get().getExplanation());
                }
            }
        }

        if (foundDupes == 0) {
            System.out.println("[INFO] No duplicate report clusters detected across active items.");
        } else {
            System.out.printf("\n[SUMMARY] Detected %d potential duplicate cluster(s).\n", foundDupes);
        }
    }

    // ==========================================
    // 6. CAMPUS ANALYTICS & HEATMAP
    // ==========================================
    private void handleAnalyticsHeatmap() {
        System.out.println(analyticsService.generateHeatmapDashboard());
    }

    // ==========================================
    // 7. UNCLAIMED RESCUE PRIORITY
    // ==========================================
    private void handleUnclaimedRescue() {
        System.out.println("\n==================================================");
        System.out.println("    SMART EXPIRY & UNCLAIMED RESCUE PRIORITY     ");
        System.out.println("==================================================");

        List<RescueService.RescueCandidate> list = rescueService.getUnclaimedPriorityList();
        if (list.isEmpty()) {
            System.out.println(" No unclaimed found items currently registered.");
            return;
        }

        System.out.printf("%-10s | %-20s | %-12s | %-10s | %-12s | %-35s\n",
                "ITEM ID", "NAME", "FOUND DATE", "AGE (DAYS)", "STATUS", "RECOMMENDED ACTION");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

        for (RescueService.RescueCandidate rc : list) {
            FoundItem item = rc.getItem();
            String name = item.getName().length() > 20 ? item.getName().substring(0, 17) + "..." : item.getName();
            String action = rc.getRecommendedAction().length() > 35 ? rc.getRecommendedAction().substring(0, 32) + "..." : rc.getRecommendedAction();

            System.out.printf("%-10s | %-20s | %-12s | %-10d | %-12s | %-35s\n",
                    item.getId(),
                    name,
                    DateUtil.formatDate(item.getDateFound()),
                    rc.getDaysElapsed(),
                    item.getStatus().name(),
                    action);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Note: IntelliRecover prioritizes overdue items without automatic destructive deletion.");
    }

    // ==========================================
    // 8. VIEW TIMELINE
    // ==========================================
    private void handleViewTimeline() {
        System.out.print("\nEnter Item ID to view its Recovery Timeline (e.g. FND-1001, LST-1001): ");
        String itemId = scanner.nextLine().trim().toUpperCase();
        System.out.println(recoveryService.formatTimeline(itemId));
    }

    // ==========================================
    // 9. VIEW AUDIT LOG
    // ==========================================
    private void handleViewAuditLog() {
        List<RecoveryEvent> events = recoveryService.getAllEvents();
        System.out.println("\n==================================================");
        System.out.printf("  SYSTEM AUDIT LOG (%d total events)\n", events.size());
        System.out.println("==================================================");

        int count = 0;
        for (RecoveryEvent ev : events) {
            System.out.printf("[%s] %-10s | %-20s | Actor: %-10s\n",
                    DateUtil.formatDateTime(ev.getTimestamp()),
                    ev.getItemId(),
                    ev.getEventType(),
                    ev.getActorUserId());
            System.out.println("   -> " + ev.getDescription());
            count++;
            if (count >= 20) {
                System.out.println("   [...Showing most recent 20 audit events...]");
                break;
            }
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private void renderLostItems(List<LostItem> items) {
        if (items.isEmpty()) {
            System.out.println(" No lost items registered.");
            return;
        }
        System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                "ID", "NAME", "CATEGORY", "LOCATION", "DATE LOST", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for (LostItem item : items) {
            String name = item.getName().length() > 22 ? item.getName().substring(0, 19) + "..." : item.getName();
            String cat = item.getCategory().getDisplayName().length() > 18 ? item.getCategory().getDisplayName().substring(0, 15) + "..." : item.getCategory().getDisplayName();
            String loc = item.getLocation().length() > 20 ? item.getLocation().substring(0, 17) + "..." : item.getLocation();
            System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                    item.getId(), name, cat, loc, DateUtil.formatDate(item.getDateLost()), item.getStatus().name());
        }
    }

    private void renderFoundItems(List<FoundItem> items) {
        if (items.isEmpty()) {
            System.out.println(" No found items registered.");
            return;
        }
        System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                "ID", "NAME", "CATEGORY", "LOCATION", "DATE FOUND", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for (FoundItem item : items) {
            String name = item.getName().length() > 22 ? item.getName().substring(0, 19) + "..." : item.getName();
            String cat = item.getCategory().getDisplayName().length() > 18 ? item.getCategory().getDisplayName().substring(0, 15) + "..." : item.getCategory().getDisplayName();
            String loc = item.getLocation().length() > 20 ? item.getLocation().substring(0, 17) + "..." : item.getLocation();
            System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                    item.getId(), name, cat, loc, DateUtil.formatDate(item.getDateFound()), item.getStatus().name());
        }
    }
}
