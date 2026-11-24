package com.intellirecover;

import com.intellirecover.exception.*;
import com.intellirecover.model.*;
import com.intellirecover.repository.*;
import com.intellirecover.service.*;
import com.intellirecover.storage.FileStorageManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Dedicated Test Suite for the Phase 1 Evaluation (November 25–28, 2026).
 * Rigorously verifies the 5 mandatory Phase 1 capabilities:
 * 1. Authentication & RBAC (Registration, Login, Role separation)
 * 2. Lost Item Reporting (Validation, Unique ID, CSV Persistence)
 * 3. Found Item Reporting (Validation, Unique ID, CSV Persistence)
 * 4. Search & Filtering (Name, Category, Location, Date, Active views)
 * 5. Basic Automatic Matching Engine (Category, Keywords, Location, Ranking, Explanations)
 */
public class Phase1TestSuite {
    private static Path tempDir;
    private static FileStorageManager storageManager;
    private static UserRepository userRepo;
    private static ItemRepository itemRepo;
    private static ClaimRepository claimRepo;
    private static HistoryRepository historyRepo;
    private static NotificationRepository notifRepo;

    private static AuthenticationService authService;
    private static MatchingService matchingService;
    private static DuplicateDetectionService duplicateService;
    private static RecoveryService recoveryService;
    private static NotificationService notifService;
    private static ItemService itemService;

    private static String lostId1;
    private static String lostId2;
    private static String foundId1;
    private static String foundId2;

    public static void setup() throws IOException {
        tempDir = Files.createTempDirectory("phase1_eval_test_");
        storageManager = new FileStorageManager(tempDir.toString());

        userRepo = new UserRepository(storageManager);
        itemRepo = new ItemRepository(storageManager);
        claimRepo = new ClaimRepository(storageManager);
        historyRepo = new HistoryRepository(storageManager);
        notifRepo = new NotificationRepository(storageManager);

        authService = new AuthenticationService(userRepo);
        matchingService = new MatchingService();
        duplicateService = new DuplicateDetectionService(matchingService);
        recoveryService = new RecoveryService(historyRepo);
        notifService = new NotificationService(notifRepo);

        itemService = new ItemService(itemRepo, matchingService, duplicateService, recoveryService, notifService);
    }

    public static void tearDown() {
        if (tempDir != null && Files.exists(tempDir)) {
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException ignored) {}
        }
    }

    // ==========================================
    // REQUIREMENT 1: AUTHENTICATION & RBAC
    // ==========================================
    public static void testPhase1_01_StudentRegistration() throws Exception {
        User u = authService.registerStudent("stu_eva", "pass2026", "Eva Green",
                "eva@university.edu", "STU-2024-555", "Biotech", "9876543210");
        assert u != null : "Student registration returned null";
        assert u.getId().startsWith("USR-") : "ID format invalid: " + u.getId();
        assert u.getRole() == UserRole.STUDENT : "Role mismatch";
        assert userRepo.findByUsername("stu_eva").isPresent() : "User not found in repo";
    }

    public static void testPhase1_02_AdminRegistrationAndLogin() throws Exception {
        User admin = authService.registerAdmin("admin_arthur", "secureAdmin1", "Dr. Arthur Vance",
                "admin@university.edu", "STF-777", "Campus Security");
        assert admin != null;
        assert admin.getRole() == UserRole.ADMINISTRATOR;

        // Verify login
        User loggedAdmin = authService.login("admin_arthur", "secureAdmin1");
        assert loggedAdmin != null;
        assert loggedAdmin.getRole() == UserRole.ADMINISTRATOR;
        assert authService.isAdmin();
        authService.logout();
        assert !authService.isLoggedIn();
    }

    public static void testPhase1_03_StudentLoginSuccessAndFailure() throws Exception {
        // Correct login
        User logged = authService.login("stu_eva", "pass2026");
        assert logged != null;
        assert "stu_eva".equals(logged.getUsername());
        assert authService.isStudent();

        // Incorrect password
        boolean authFailed = false;
        try {
            authService.login("stu_eva", "incorrectPass");
        } catch (AuthenticationException e) {
            authFailed = true;
        }
        assert authFailed : "Login should fail on incorrect password";
    }

    // ==========================================
    // REQUIREMENT 2: LOST ITEM REPORTING
    // ==========================================
    public static void testPhase1_04_LostItemReportingWithValidation() throws Exception {
        // Valid report
        LostItem item1 = itemService.reportLostItem(
                "Black Water Bottle",
                ItemCategory.WATER_BOTTLE,
                "Black stainless steel flask 750ml with university crest sticker",
                "Central Library 2nd Floor",
                LocalDate.now().minusDays(1),
                "USR-1001",
                "Milton",
                "university crest sticker",
                "Reward 50"
        );
        assert item1 != null;
        assert item1.getId().startsWith("LST-");
        assert item1.getStatus() == ItemStatus.REPORTED || item1.getStatus() == ItemStatus.MATCH_PENDING;
        lostId1 = item1.getId();

        // Second item in a different category
        LostItem item2 = itemService.reportLostItem(
                "Scientific Calculator",
                ItemCategory.ELECTRONICS,
                "Casio fx-991EX ClassWiz with initials on back cover",
                "Engineering Block Room 204",
                LocalDate.now().minusDays(3),
                "USR-1001",
                "Casio",
                "blue initials sticker",
                ""
        );
        assert item2 != null;
        lostId2 = item2.getId();

        // Test input validation: Empty name should throw InvalidReportException
        boolean emptyNameFailed = false;
        try {
            itemService.reportLostItem("", ItemCategory.OTHER, "Valid description text", "Library", LocalDate.now(), "USR-1001", "", "", "");
        } catch (InvalidReportException e) {
            emptyNameFailed = true;
        }
        assert emptyNameFailed : "Empty item name should be rejected by validation";

        // Test input validation: Future date should throw InvalidReportException
        boolean futureDateFailed = false;
        try {
            itemService.reportLostItem("Future Item", ItemCategory.OTHER, "Valid description text", "Library", LocalDate.now().plusDays(5), "USR-1001", "", "", "");
        } catch (InvalidReportException e) {
            futureDateFailed = true;
        }
        assert futureDateFailed : "Future incident date should be rejected";
    }

    // ==========================================
    // REQUIREMENT 3: FOUND ITEM REPORTING
    // ==========================================
    public static void testPhase1_05_FoundItemReportingWithValidation() throws Exception {
        FoundItem f1 = itemService.reportFoundItem(
                "Black Sports Bottle",
                ItemCategory.WATER_BOTTLE,
                "Black stainless steel sports bottle found under study carrel",
                "Central Library 2nd Floor",
                LocalDate.now().minusDays(1),
                "USR-1002",
                "Milton",
                "university crest sticker",
                "Library Reception",
                Collections.emptyList()
        );
        assert f1 != null;
        assert f1.getId().startsWith("FND-");
        assert f1.getStatus() == ItemStatus.REPORTED || f1.getStatus() == ItemStatus.MATCH_PENDING;
        foundId1 = f1.getId();

        FoundItem f2 = itemService.reportFoundItem(
                "Casio Calculator",
                ItemCategory.ELECTRONICS,
                "Casio scientific calculator found on hallway bench",
                "Engineering Block Corridor",
                LocalDate.now().minusDays(2),
                "USR-1002",
                "Casio",
                "initials sticker",
                "Engineering Dept Office",
                Collections.emptyList()
        );
        assert f2 != null;
        foundId2 = f2.getId();
    }

    // ==========================================
    // REQUIREMENT 4: SEARCH & FILTERING
    // ==========================================
    public static void testPhase1_06_SearchByNameAndKeyword() {
        List<LostItem> byName = itemService.searchLostItems("bottle", null, null, null, null);
        assert !byName.isEmpty() : "Search by keyword 'bottle' should find lost bottle";
        assert byName.get(0).getName().toLowerCase().contains("bottle");

        List<FoundItem> foundByName = itemService.searchFoundItems("calculator", null, null, null, null);
        assert !foundByName.isEmpty() : "Search by keyword 'calculator' should find found calculator";
    }

    public static void testPhase1_07_SearchByCategory() {
        List<LostItem> bottleLost = itemService.searchLostItems(null, ItemCategory.WATER_BOTTLE, null, null, null);
        assert !bottleLost.isEmpty();
        assert bottleLost.stream().allMatch(i -> i.getCategory() == ItemCategory.WATER_BOTTLE);

        List<FoundItem> electronicsFound = itemService.searchFoundItems(null, ItemCategory.ELECTRONICS, null, null, null);
        assert !electronicsFound.isEmpty();
        assert electronicsFound.stream().allMatch(i -> i.getCategory() == ItemCategory.ELECTRONICS);
    }

    public static void testPhase1_08_SearchByLocation() {
        List<LostItem> libraryLost = itemService.searchLostItems(null, null, "Library", null, null);
        assert !libraryLost.isEmpty();
        assert libraryLost.get(0).getLocation().contains("Library");
    }

    public static void testPhase1_09_SearchByDateRange() {
        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate end = LocalDate.now();

        List<LostItem> inRange = itemService.searchLostItems(null, null, null, start, end);
        assert !inRange.isEmpty();
        for (LostItem item : inRange) {
            assert !item.getDateLost().isBefore(start) && !item.getDateLost().isAfter(end);
        }
    }

    public static void testPhase1_10_ViewActiveReports() {
        List<LostItem> activeLost = itemRepo.getActiveLostItems();
        assert activeLost.size() >= 2 : "Should have at least 2 active lost items";

        List<FoundItem> activeFound = itemRepo.getActiveFoundItems();
        assert activeFound.size() >= 2 : "Should have at least 2 active found items";
    }

    // ==========================================
    // REQUIREMENT 5: BASIC AUTOMATIC MATCHING
    // ==========================================
    public static void testPhase1_11_MatchingScoringAndExplanation() {
        LostItem lost = itemRepo.findLostItemById(lostId1).get();
        FoundItem found = itemRepo.findFoundItemById(foundId1).get();

        MatchResult match = matchingService.calculateMatch(lost, found);
        assert match != null;
        assert match.getTotalScore() >= 60.0 : "Match score should be significant: " + match.getTotalScore();
        assert match.getCategoryScore() == 20.0 : "Category should award 20 points";
        assert match.getLocationScore() == 20.0 : "Same library location should award 20 points";
        assert match.getDateScore() >= 13.0 : "Same or 1-day proximity should award points";
        assert !match.getReasons().isEmpty() : "Explanation reasons must be provided";

        // Verify human-readable score explanation
        String explanation = match.getFormattedExplanation();
        assert explanation.contains("Reasons:") : "Explanation must contain breakdown";
        assert explanation.contains("Same category") : "Must explain category point award";
        assert explanation.contains("Match Score:") : "Must display final score";
    }

    public static void testPhase1_12_MatchRankingDescending() {
        LostItem lost = itemRepo.findLostItemById(lostId1).get();
        List<FoundItem> candidates = itemRepo.getAllFoundItems();

        List<MatchResult> ranked = matchingService.findMatchesForLostItem(lost, candidates);
        assert ranked.size() >= 1;

        // Verify descending score order
        for (int i = 0; i < ranked.size() - 1; i++) {
            assert ranked.get(i).getTotalScore() >= ranked.get(i + 1).getTotalScore() :
                    "Matches must be ranked in descending score order!";
        }
    }

    // ==========================================
    // PERSISTENCE VERIFICATION FOR PHASE 1
    // ==========================================
    public static void testPhase1_13_CSVPersistenceAcrossReload() throws Exception {
        // Re-read directly from disk using new repository instances
        UserRepository freshUserRepo = new UserRepository(storageManager);
        ItemRepository freshItemRepo = new ItemRepository(storageManager);

        assert freshUserRepo.findByUsername("stu_eva").isPresent() : "Registered student not persisted in users.csv";
        assert freshItemRepo.findLostItemById(lostId1).isPresent() : "Lost item not persisted in lost_items.csv";
        assert freshItemRepo.findFoundItemById(foundId1).isPresent() : "Found item not persisted in found_items.csv";
    }
}
