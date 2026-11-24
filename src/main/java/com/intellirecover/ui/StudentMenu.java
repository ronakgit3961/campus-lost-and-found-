package com.intellirecover.ui;

import com.intellirecover.exception.IntelliRecoverException;
import com.intellirecover.model.*;
import com.intellirecover.service.*;
import com.intellirecover.util.DateUtil;
import com.intellirecover.util.FingerprintUtil;
import com.intellirecover.util.InputValidator;

import java.time.LocalDate;
import java.util.*;

/**
 * Terminal UI controller for authenticated Student actions.
 */
public class StudentMenu {
    private final Scanner scanner;
    private final AuthenticationService authService;
    private final ItemService itemService;
    private final MatchingService matchingService;
    private final ClaimService claimService;
    private final RecoveryService recoveryService;
    private final NotificationService notificationService;

    public StudentMenu(Scanner scanner,
                       AuthenticationService authService,
                       ItemService itemService,
                       MatchingService matchingService,
                       ClaimService claimService,
                       RecoveryService recoveryService,
                       NotificationService notificationService) {
        this.scanner = scanner;
        this.authService = authService;
        this.itemService = itemService;
        this.matchingService = matchingService;
        this.claimService = claimService;
        this.recoveryService = recoveryService;
        this.notificationService = notificationService;
    }

    public void run() {
        boolean active = true;
        while (active && authService.isLoggedIn()) {
            User current = authService.getCurrentUser();
            long unread = notificationService.getUnreadCount(current.getId());
            String notifBadge = unread > 0 ? String.format(" [%d UNREAD]", unread) : "";

            System.out.println("\n==================================================");
            System.out.println("             STUDENT DASHBOARD");
            System.out.println("  " + current.getRoleHeader());
            System.out.println("==================================================");
            System.out.println(" 1. Report Lost Item");
            System.out.println(" 2. Report Found Item");
            System.out.println(" 3. Search Items Catalog");
            System.out.println(" 4. View My Reported Items");
            System.out.println(" 5. Find Automatic Matches (Smart Match Engine)");
            System.out.println(" 6. Submit Ownership Claim (Verification Challenge)");
            System.out.println(" 7. Track My Claims Status");
            System.out.println(" 8. View Recovery Timeline");
            System.out.println(" 9. View Notifications" + notifBadge);
            System.out.println(" 10. Logout");
            System.out.println("==================================================");
            System.out.print("Enter your choice (1-10): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleReportLost();
                case "2" -> handleReportFound();
                case "3" -> handleSearch();
                case "4" -> handleViewMyReports();
                case "5" -> handleFindMatches();
                case "6" -> handleSubmitClaim();
                case "7" -> handleTrackClaims();
                case "8" -> handleViewTimeline();
                case "9" -> handleNotifications();
                case "10" -> {
                    authService.logout();
                    System.out.println("\n[SUCCESS] Logged out successfully. Returning to main menu.");
                    active = false;
                }
                default -> System.out.println("\n[ERROR] Invalid option. Please enter a number between 1 and 10.");
            }
        }
    }

    // ==========================================
    // 1. REPORT LOST ITEM
    // ==========================================
    private void handleReportLost() {
        System.out.println("\n--- REPORT LOST ITEM ---");
        System.out.print("Item Name (e.g. Black Water Bottle): ");
        String name = scanner.nextLine().trim();

        ItemCategory category = promptCategory();

        System.out.print("Detailed Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Campus Location Lost (e.g. Central Library 2nd Floor): ");
        String location = scanner.nextLine().trim();

        LocalDate dateLost = promptDate("Date Lost (yyyy-MM-dd, press Enter for Today): ");

        System.out.print("Brand / Manufacturer (optional, e.g. Milton, Apple): ");
        String brand = scanner.nextLine().trim();

        System.out.print("Distinctive Attributes (optional, e.g. university sticker, dent on bottom): ");
        String attributes = scanner.nextLine().trim();

        System.out.print("Reward or Recovery Notes (optional): ");
        String reward = scanner.nextLine().trim();

        // Prepare candidate for duplicate check
        Set<String> keywords = matchingService.extractKeywords(name + " " + description);
        String fp = FingerprintUtil.generateFingerprint(category.name(), name, brand, keywords, attributes);
        LostItem candidate = new LostItem(null, name, category, description, location,
                LocalDate.now(), ItemStatus.REPORTED, authService.getCurrentUser().getId(),
                fp, brand, attributes, dateLost, reward);

        // FEATURE 4: Duplicate Report Detection
        Optional<DuplicateDetectionService.DuplicateWarning> warning = itemService.checkLostDuplicate(candidate);
        if (warning.isPresent()) {
            DuplicateDetectionService.DuplicateWarning w = warning.get();
            System.out.println("\n**************************************************");
            System.out.println("  WARNING: A potentially similar report already exists!");
            System.out.printf("  Existing Report ID : %s (%s)\n", w.getExistingItem().getId(), w.getExistingItem().getName());
            System.out.printf("  Similarity Score   : %.0f%%\n", w.getSimilarityPercentage());
            System.out.printf("  Details            : %s\n", w.getExplanation());
            System.out.println("**************************************************");
            System.out.println("1. View existing report");
            System.out.println("2. Continue creating new report anyway");
            System.out.println("3. Cancel report");
            System.out.print("Choose action (1-3): ");
            String dupChoice = scanner.nextLine().trim();
            if ("1".equals(dupChoice)) {
                displayItemDetails(w.getExistingItem());
                System.out.print("Do you still want to proceed with your new report? (y/n): ");
                String proceed = scanner.nextLine().trim();
                if (!proceed.equalsIgnoreCase("y")) {
                    System.out.println("[INFO] Report cancelled to prevent duplication.");
                    return;
                }
            } else if ("3".equals(dupChoice)) {
                System.out.println("[INFO] Report cancelled.");
                return;
            }
        }

        try {
            LostItem saved = itemService.reportLostItem(name, category, description, location,
                    dateLost, authService.getCurrentUser().getId(), brand, attributes, reward);

            System.out.println("\n[SUCCESS] Lost item registered successfully!");
            System.out.printf("Report ID : %s\n", saved.getId());
            System.out.printf("Item DNA  : %s\n", FingerprintUtil.getShortFingerprint(saved.getFingerprint()));
            System.out.printf("Status    : %s\n", saved.getStatus().getDescription());
        } catch (IntelliRecoverException e) {
            System.out.println("\n[ERROR] Failed to save report: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. REPORT FOUND ITEM
    // ==========================================
    private void handleReportFound() {
        System.out.println("\n--- REPORT FOUND ITEM ---");
        System.out.print("Item Name (e.g. Black Sports Bottle): ");
        String name = scanner.nextLine().trim();

        ItemCategory category = promptCategory();

        System.out.print("Public Description (do NOT include secret identifying details): ");
        String description = scanner.nextLine().trim();

        System.out.print("Campus Location Found (e.g. Central Library 2nd Floor): ");
        String location = scanner.nextLine().trim();

        LocalDate dateFound = promptDate("Date Found (yyyy-MM-dd, press Enter for Today): ");

        System.out.print("Brand / Manufacturer (optional, e.g. Milton): ");
        String brand = scanner.nextLine().trim();

        System.out.print("Public Distinctive Attributes (e.g. stainless steel): ");
        String attributes = scanner.nextLine().trim();

        System.out.print("Current Storage Location (e.g. Library Front Desk Room 102): ");
        String storageLoc = scanner.nextLine().trim();
        if (storageLoc.isEmpty()) storageLoc = "Campus Security Desk";

        // FEATURE 2: Secret Verification Questions setup
        System.out.println("\n[FEATURE 2] SECRET OWNERSHIP VERIFICATION SETUP");
        System.out.println("You can configure private verification questions that true owners must answer to claim this item.");
        System.out.println("Examples: 'What sticker is on the back?', 'What is the color of the case?'");
        System.out.print("Would you like to add secret verification questions? (y/n): ");
        String addQ = scanner.nextLine().trim();

        List<VerificationQuestion> secretQuestions = new ArrayList<>();
        if (addQ.equalsIgnoreCase("y")) {
            boolean adding = true;
            int qNum = 1;
            while (adding) {
                System.out.printf("Question #%d: ", qNum);
                String q = scanner.nextLine().trim();
                System.out.printf("Expected Secret Answer #%d: ", qNum);
                String a = scanner.nextLine().trim();
                if (!q.isEmpty() && !a.isEmpty()) {
                    secretQuestions.add(new VerificationQuestion(q, a));
                    qNum++;
                }
                System.out.print("Add another secret question? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    adding = false;
                }
            }
        }

        // Prepare candidate for duplicate check
        Set<String> keywords = matchingService.extractKeywords(name + " " + description);
        String fp = FingerprintUtil.generateFingerprint(category.name(), name, brand, keywords, attributes);
        FoundItem candidate = new FoundItem(null, name, category, description, location,
                LocalDate.now(), ItemStatus.REPORTED, authService.getCurrentUser().getId(),
                fp, brand, attributes, dateFound, storageLoc, secretQuestions, null);

        // FEATURE 4: Duplicate check
        Optional<DuplicateDetectionService.DuplicateWarning> warning = itemService.checkFoundDuplicate(candidate);
        if (warning.isPresent()) {
            DuplicateDetectionService.DuplicateWarning w = warning.get();
            System.out.println("\n**************************************************");
            System.out.println("  WARNING: A potentially similar found report already exists!");
            System.out.printf("  Existing Report ID : %s (%s)\n", w.getExistingItem().getId(), w.getExistingItem().getName());
            System.out.printf("  Similarity Score   : %.0f%%\n", w.getSimilarityPercentage());
            System.out.printf("  Details            : %s\n", w.getExplanation());
            System.out.println("**************************************************");
            System.out.println("1. View existing report");
            System.out.println("2. Continue creating new report anyway");
            System.out.println("3. Cancel report");
            System.out.print("Choose action (1-3): ");
            String dupChoice = scanner.nextLine().trim();
            if ("1".equals(dupChoice)) {
                displayItemDetails(w.getExistingItem());
                System.out.print("Do you still want to proceed with your new report? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    System.out.println("[INFO] Report cancelled.");
                    return;
                }
            } else if ("3".equals(dupChoice)) {
                System.out.println("[INFO] Report cancelled.");
                return;
            }
        }

        try {
            FoundItem saved = itemService.reportFoundItem(name, category, description, location,
                    dateFound, authService.getCurrentUser().getId(), brand, attributes,
                    storageLoc, secretQuestions);

            System.out.println("\n[SUCCESS] Found item registered successfully!");
            System.out.printf("Report ID      : %s\n", saved.getId());
            System.out.printf("Item DNA       : %s\n", FingerprintUtil.getShortFingerprint(saved.getFingerprint()));
            System.out.printf("Storage Vault  : %s\n", saved.getStorageLocation());
            System.out.printf("Secret Qs Set  : %d question(s) configured (Hidden from public)\n", saved.getSecretQuestions().size());
        } catch (IntelliRecoverException e) {
            System.out.println("\n[ERROR] Failed to save found item: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. SEARCH ITEMS
    // ==========================================
    private void handleSearch() {
        System.out.println("\n--- SEARCH ITEMS CATALOG ---");
        System.out.print("Search Lost items or Found items? (1 = Lost, 2 = Found, Enter = Both): ");
        String typeChoice = scanner.nextLine().trim();

        System.out.print("Keyword / Name search (press Enter to skip): ");
        String query = scanner.nextLine().trim();

        System.out.print("Filter by Category? (y/n): ");
        ItemCategory category = scanner.nextLine().trim().equalsIgnoreCase("y") ? promptCategory() : null;

        System.out.print("Filter by Location keyword (press Enter to skip): ");
        String location = scanner.nextLine().trim();

        if ("1".equals(typeChoice) || typeChoice.isEmpty()) {
            List<LostItem> lostResults = itemService.searchLostItems(query, category, location, null, null);
            System.out.printf("\n--- LOST ITEMS RESULTS (%d found) ---\n", lostResults.size());
            renderLostItemsTable(lostResults);
        }

        if ("2".equals(typeChoice) || typeChoice.isEmpty()) {
            List<FoundItem> foundResults = itemService.searchFoundItems(query, category, location, null, null);
            System.out.printf("\n--- FOUND ITEMS RESULTS (%d found) ---\n", foundResults.size());
            renderFoundItemsTable(foundResults);
        }
    }

    // ==========================================
    // 4. VIEW MY REPORTS
    // ==========================================
    private void handleViewMyReports() {
        String uid = authService.getCurrentUser().getId();
        List<LostItem> myLost = itemService.getLostItemsByUser(uid);
        List<FoundItem> myFound = itemService.getFoundItemsByUser(uid);

        System.out.println("\n==================================================");
        System.out.printf("  MY REPORTED ITEMS (%d Lost, %d Found)\n", myLost.size(), myFound.size());
        System.out.println("==================================================");

        System.out.println("\n--- MY LOST ITEMS ---");
        renderLostItemsTable(myLost);

        System.out.println("\n--- MY FOUND ITEMS ---");
        renderFoundItemsTable(myFound);
    }

    // ==========================================
    // 5. AUTOMATIC MATCH ENGINE
    // ==========================================
    private void handleFindMatches() {
        String uid = authService.getCurrentUser().getId();
        List<LostItem> myLost = itemService.getLostItemsByUser(uid);

        if (myLost.isEmpty()) {
            System.out.println("\n[INFO] You have no reported lost items yet. Please report a lost item first.");
            return;
        }

        System.out.println("\n--- SELECT ONE OF YOUR LOST ITEMS TO MATCH ---");
        for (int i = 0; i < myLost.size(); i++) {
            LostItem item = myLost.get(i);
            System.out.printf(" [%d] %s: %s (Category: %s, Status: %s)\n",
                    i + 1, item.getId(), item.getName(), item.getCategory().getDisplayName(), item.getStatus().getDescription());
        }
        System.out.print("Select item number (1-" + myLost.size() + "): ");
        int sel = InputValidator.parsePositiveInt(scanner.nextLine().trim(), -1);
        if (sel < 1 || sel > myLost.size()) {
            System.out.println("[ERROR] Invalid selection.");
            return;
        }

        LostItem chosen = myLost.get(sel - 1);
        List<FoundItem> activeFound = itemService.getAllFoundItems();

        System.out.println("\n[RUNNING SMART MATCH ENGINE] Scanning campus found items...");
        List<MatchResult> matches = matchingService.findMatchesForLostItem(chosen, activeFound);

        if (matches.isEmpty()) {
            System.out.println("[INFO] No matches currently exceed the minimum display threshold (Score >= 40/100).");
            return;
        }

        System.out.printf("\nFound %d potential match candidate(s) (Ranked by Score):\n", matches.size());
        for (int i = 0; i < matches.size(); i++) {
            MatchResult mr = matches.get(i);
            System.out.println("--------------------------------------------------");
            System.out.printf("Match #%d: [%s] (Score: %.0f/100)\n", i + 1, mr.getTier().getLabel(), mr.getTotalScore());
            System.out.println("--------------------------------------------------");
            System.out.println(mr.getFormattedExplanation());
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Tip: Use Option 6 (Submit Ownership Claim) to challenge and recover any matching found item.");
    }

    // ==========================================
    // 6. SUBMIT OWNERSHIP CLAIM
    // ==========================================
    private void handleSubmitClaim() {
        System.out.println("\n--- SUBMIT OWNERSHIP CLAIM ---");
        System.out.print("Enter Found Item ID to Claim (e.g. FND-1001): ");
        String foundId = scanner.nextLine().trim().toUpperCase();

        Optional<FoundItem> optFound = itemService.getFoundItemById(foundId);
        if (optFound.isEmpty()) {
            System.out.println("[ERROR] Found item with ID '" + foundId + "' not found.");
            return;
        }

        FoundItem item = optFound.get();
        System.out.println("\nItem Claim Target: " + item.getName() + " (" + item.getCategory().getDisplayName() + ")");
        System.out.println("Location Found   : " + item.getLocation());
        System.out.println("Storage Custody  : " + item.getStorageLocation());

        List<String> answers = new ArrayList<>();
        if (item.hasSecretQuestions()) {
            System.out.println("\n**************************************************");
            System.out.println("  SECRET OWNERSHIP VERIFICATION CHALLENGE");
            System.out.println("  Please answer the private identifying questions.");
            System.out.println("**************************************************");
            List<VerificationQuestion> questions = item.getSecretQuestions();
            for (int i = 0; i < questions.size(); i++) {
                System.out.printf("Q%d: %s\n", i + 1, questions.get(i).getQuestion());
                System.out.print("Your Answer: ");
                answers.add(scanner.nextLine().trim());
            }
        } else {
            System.out.println("\n[NOTE] No secret verification questions were configured by the finder.");
            System.out.print("Provide your proof of ownership or identifying notes for admin review: ");
            answers.add(scanner.nextLine().trim());
        }

        try {
            Claim claim = claimService.submitClaim(foundId, authService.getCurrentUser().getId(), answers);
            System.out.println("\n[RESULT] Claim processed!");
            System.out.printf("Claim ID            : %s\n", claim.getId());
            System.out.printf("Verification Score  : %.1f%%\n", claim.getVerificationScore());
            System.out.printf("Claim Status        : %s\n", claim.getStatus().getDescription());
            System.out.printf("Notes               : %s\n", claim.getReviewNotes());
        } catch (IntelliRecoverException e) {
            System.out.println("\n[ERROR] Claim submission blocked: " + e.getMessage());
        }
    }

    // ==========================================
    // 7. TRACK MY CLAIMS
    // ==========================================
    private void handleTrackClaims() {
        String uid = authService.getCurrentUser().getId();
        List<Claim> claims = claimService.getClaimsForUser(uid);

        System.out.println("\n==================================================");
        System.out.printf("  MY OWNERSHIP CLAIMS (%d total)\n", claims.size());
        System.out.println("==================================================");

        if (claims.isEmpty()) {
            System.out.println(" You have not submitted any claims yet.");
            return;
        }

        System.out.printf("%-10s | %-10s | %-12s | %-10s | %-18s\n",
                "CLAIM ID", "ITEM ID", "SUBMITTED", "VERIF. %", "STATUS");
        System.out.println("----------------------------------------------------------------------");
        for (Claim c : claims) {
            System.out.printf("%-10s | %-10s | %-12s | %-9.1f%% | %-18s\n",
                    c.getId(),
                    c.getFoundItemId(),
                    DateUtil.formatDateTime(c.getSubmissionDate()).substring(0, 10),
                    c.getVerificationScore(),
                    c.getStatus().name());
            if (!c.getReviewNotes().isEmpty()) {
                System.out.printf("   └─ Notes: %s\n", c.getReviewNotes());
            }
        }
    }

    // ==========================================
    // 8. RECOVERY TIMELINE
    // ==========================================
    private void handleViewTimeline() {
        System.out.print("\nEnter Item ID to view its Recovery Timeline (e.g. FND-1001, LST-1001): ");
        String itemId = scanner.nextLine().trim().toUpperCase();
        System.out.println(recoveryService.formatTimeline(itemId));
    }

    // ==========================================
    // 9. NOTIFICATIONS
    // ==========================================
    private void handleNotifications() {
        String uid = authService.getCurrentUser().getId();
        List<Notification> notifs = notificationService.getNotificationsForUser(uid);

        System.out.println("\n==================================================");
        System.out.printf("  MY NOTIFICATIONS & ALERTS (%d total)\n", notifs.size());
        System.out.println("==================================================");

        if (notifs.isEmpty()) {
            System.out.println(" No notifications on record.");
            return;
        }

        for (Notification n : notifs) {
            String status = n.isRead() ? "[READ]" : "[NEW!]";
            System.out.printf("%s %s | %s\n", status, DateUtil.formatDateTime(n.getTimestamp()), n.getTitle());
            System.out.println("   -> " + n.getMessage());
        }

        notificationService.markAllAsRead(uid);
        System.out.println("\n[INFO] All notifications marked as read.");
    }

    // ==========================================
    // UI HELPER METHODS
    // ==========================================
    private ItemCategory promptCategory() {
        System.out.println("\nSelect Item Category:");
        ItemCategory[] categories = ItemCategory.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf(" [%d] %s\n", i + 1, categories[i].getDisplayName());
        }
        System.out.print("Choose category (1-" + categories.length + "): ");
        int sel = InputValidator.parsePositiveInt(scanner.nextLine().trim(), categories.length);
        if (sel < 1 || sel > categories.length) sel = categories.length;
        return categories[sel - 1];
    }

    private LocalDate promptDate(String prompt) {
        System.out.print(prompt);
        String text = scanner.nextLine().trim();
        if (text.isEmpty()) {
            return LocalDate.now();
        }
        LocalDate parsed = DateUtil.parseDate(text);
        if (parsed == null || parsed.isAfter(LocalDate.now())) {
            System.out.println("[WARN] Invalid or future date. Defaulting to today's date.");
            return LocalDate.now();
        }
        return parsed;
    }

    private void renderLostItemsTable(List<LostItem> items) {
        if (items.isEmpty()) {
            System.out.println(" No lost items found matching criteria.");
            return;
        }
        System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                "ID", "NAME", "CATEGORY", "LOCATION", "DATE LOST", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for (LostItem item : items) {
            String name = truncate(item.getName(), 22);
            String cat = truncate(item.getCategory().getDisplayName(), 18);
            String loc = truncate(item.getLocation(), 20);
            System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                    item.getId(), name, cat, loc, DateUtil.formatDate(item.getDateLost()), item.getStatus().name());
        }
    }

    private void renderFoundItemsTable(List<FoundItem> items) {
        if (items.isEmpty()) {
            System.out.println(" No found items found matching criteria.");
            return;
        }
        System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                "ID", "NAME", "CATEGORY", "LOCATION", "DATE FOUND", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for (FoundItem item : items) {
            String name = truncate(item.getName(), 22);
            String cat = truncate(item.getCategory().getDisplayName(), 18);
            String loc = truncate(item.getLocation(), 20);
            System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-14s\n",
                    item.getId(), name, cat, loc, DateUtil.formatDate(item.getDateFound()), item.getStatus().name());
        }
    }

    private void displayItemDetails(Item item) {
        System.out.println("\n--------------------------------------------------");
        System.out.printf("ITEM DETAILS: %s (%s)\n", item.getName(), item.getId());
        System.out.println("--------------------------------------------------");
        System.out.printf("Category    : %s\n", item.getCategory().getDisplayName());
        System.out.printf("Location    : %s\n", item.getLocation());
        System.out.printf("Description : %s\n", item.getDescription());
        System.out.printf("Brand       : %s\n", item.getBrand().isEmpty() ? "N/A" : item.getBrand());
        System.out.printf("Attributes  : %s\n", item.getDistinctiveAttributes().isEmpty() ? "N/A" : item.getDistinctiveAttributes());
        System.out.printf("Reported    : %s\n", DateUtil.formatDate(item.getReportedDate()));
        System.out.printf("Status      : %s\n", item.getStatus().getDescription());
        System.out.println("--------------------------------------------------");
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
