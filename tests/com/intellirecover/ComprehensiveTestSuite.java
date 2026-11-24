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
 * Rigorous 20-point automated test suite validating all mandatory course concepts,
 * algorithms, collections, and security constraints.
 */
public class ComprehensiveTestSuite {
    private static Path testDir;
    private static FileStorageManager storageManager;
    private static UserRepository userRepo;
    private static ItemRepository itemRepo;
    private static ClaimRepository claimRepo;
    private static HistoryRepository historyRepo;
    private static NotificationRepository notifRepo;

    private static AuthenticationService authService;
    private static MatchingService matchingService;
    private static DuplicateDetectionService duplicateService;
    private static VerificationService verificationService;
    private static RecoveryService recoveryService;
    private static NotificationService notifService;
    private static ItemService itemService;
    private static ClaimService claimService;

    private static String lostId;
    private static String foundId;

    public static void setup() throws IOException {
        testDir = Files.createTempDirectory("intellirecover_test_");
        storageManager = new FileStorageManager(testDir.toString());

        userRepo = new UserRepository(storageManager);
        itemRepo = new ItemRepository(storageManager);
        claimRepo = new ClaimRepository(storageManager);
        historyRepo = new HistoryRepository(storageManager);
        notifRepo = new NotificationRepository(storageManager);

        authService = new AuthenticationService(userRepo);
        matchingService = new MatchingService();
        duplicateService = new DuplicateDetectionService(matchingService);
        verificationService = new VerificationService();
        recoveryService = new RecoveryService(historyRepo);
        notifService = new NotificationService(notifRepo);

        itemService = new ItemService(itemRepo, matchingService, duplicateService, recoveryService, notifService);
        claimService = new ClaimService(claimRepo, itemRepo, verificationService, recoveryService, notifService);
    }

    public static void tearDown() {
        if (testDir != null && Files.exists(testDir)) {
            try {
                Files.walk(testDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException ignored) {}
        }
    }

    // 01. Student Registration
    public static void test01_StudentRegistration() throws Exception {
        User u = authService.registerStudent("tester1", "pass1234", "Test Student",
                "test@campus.edu", "STU-9901", "CS", "9998887770");
        assert u != null : "User was null";
        assert u.getId().startsWith("USR-") : "Invalid ID prefix: " + u.getId();
        assert u.getRole() == UserRole.STUDENT;
        assert userRepo.findByUsername("tester1").isPresent();
    }

    // 02. Login Correct Credentials
    public static void test02_LoginCorrect() throws Exception {
        User logged = authService.login("tester1", "pass1234");
        assert logged != null;
        assert "tester1".equals(logged.getUsername());
        assert authService.isLoggedIn();
    }

    // 03. Login Incorrect Credentials
    public static void test03_LoginIncorrect() {
        boolean failed = false;
        try {
            authService.login("tester1", "wrongpassword");
        } catch (AuthenticationException e) {
            failed = true;
        }
        assert failed : "Expected AuthenticationException on invalid password";
    }

    // 04. Lost Item Reporting
    public static void test04_LostItemReporting() throws Exception {
        LostItem item = itemService.reportLostItem(
                "Black Water Bottle", ItemCategory.WATER_BOTTLE,
                "Black stainless steel sports bottle with university crest sticker",
                "Central Library 2nd Floor", LocalDate.now(), "USR-1001",
                "Milton", "university crest sticker, dent on bottom", "50 reward"
        );
        assert item != null;
        assert item.getId().startsWith("LST-");
        assert item.getFingerprint() != null && !item.getFingerprint().isEmpty();
        assert itemRepo.findLostItemById(item.getId()).isPresent();
        lostId = item.getId();
    }

    // 05. Found Item Reporting
    public static void test05_FoundItemReporting() throws Exception {
        List<VerificationQuestion> questions = Arrays.asList(
                new VerificationQuestion("What sticker is on the bottle?", "university crest"),
                new VerificationQuestion("What dent is on the bottom?", "small dent")
        );
        FoundItem item = itemService.reportFoundItem(
                "Black Sports Bottle", ItemCategory.WATER_BOTTLE,
                "Black stainless steel sports bottle found in library",
                "Central Library 2nd Floor", LocalDate.now(), "USR-1002",
                "Milton", "university crest sticker, dent on bottom",
                "Library Reception", questions
        );
        assert item != null;
        assert item.getId().startsWith("FND-");
        assert item.hasSecretQuestions();
        assert item.getSecretQuestions().size() == 2;
        // Verify answer is not revealed in toString
        assert !item.getSecretQuestions().get(0).toString().contains("university crest");
        foundId = item.getId();
    }

    // 06. Searching by Category
    public static void test06_SearchByCategory() {
        List<LostItem> results = itemService.searchLostItems(null, ItemCategory.WATER_BOTTLE, null, null, null);
        assert !results.isEmpty() : "Should find reported water bottle";
        for (LostItem i : results) {
            assert i.getCategory() == ItemCategory.WATER_BOTTLE;
        }
    }

    // 07. Searching by Location
    public static void test07_SearchByLocation() {
        List<FoundItem> results = itemService.searchFoundItems(null, null, "Library", null, null);
        assert !results.isEmpty() : "Should find library found bottle";
        assert results.get(0).getLocation().contains("Library");
    }

    // 08. Automatic Match Scoring
    public static void test08_AutomaticMatchScoring() {
        LostItem lost = itemRepo.findLostItemById(lostId).get();
        FoundItem found = itemRepo.findFoundItemById(foundId).get();

        MatchResult result = matchingService.calculateMatch(lost, found);
        assert result.getTotalScore() >= 80.0 : "Expected Strong Match >= 80, got " + result.getTotalScore();
        assert result.getTier() == MatchTier.STRONG_MATCH;
        assert result.getCategoryScore() == 20.0;
        assert result.getLocationScore() == 20.0;
        assert result.getDateScore() >= 13.0;
        assert !result.getReasons().isEmpty();
    }

    // 09. Match Ranking with PriorityQueue
    public static void test09_MatchRanking() throws Exception {
        LostItem lost = itemRepo.findLostItemById(lostId).get();

        // Add a second, moderately matching found item in same building
        FoundItem secondItem = itemService.reportFoundItem(
                "Stainless Water Bottle", ItemCategory.WATER_BOTTLE,
                "Silver stainless steel bottle found in library reading hall",
                "Central Library 1st Floor", LocalDate.now().minusDays(1), "USR-1003",
                "Milton", "", "Library Desk", Collections.emptyList()
        );

        List<MatchResult> ranked = matchingService.findMatchesForLostItem(lost, itemRepo.getAllFoundItems());
        assert ranked.size() >= 2 : "Expected at least 2 ranked matches";
        assert ranked.get(0).getTotalScore() >= ranked.get(1).getTotalScore() : "Ranking order violated";
    }

    // 10. Duplicate Report Detection
    public static void test10_DuplicateDetection() {
        LostItem existing = itemRepo.findLostItemById(lostId).get();

        LostItem candidate = new LostItem(null, existing.getName(), existing.getCategory(),
                existing.getDescription(), existing.getLocation(), LocalDate.now(),
                ItemStatus.REPORTED, "USR-1004", existing.getFingerprint(),
                existing.getBrand(), existing.getDistinctiveAttributes(), LocalDate.now(), "");

        Optional<DuplicateDetectionService.DuplicateWarning> warning = itemService.checkLostDuplicate(candidate);
        assert warning.isPresent() : "Expected duplicate warning";
        assert warning.get().getSimilarityPercentage() >= 70.0;
        assert warning.get().isFingerprintMatch();
    }

    // 11. Secret Ownership Verification Valid
    public static void test11_SecretVerificationValid() {
        FoundItem found = itemRepo.findFoundItemById(foundId).get();
        List<String> goodAnswers = Arrays.asList("university crest", "small dent");

        double score = verificationService.evaluateAnswers(found, goodAnswers);
        assert score == 100.0 : "Expected 100% verification, got " + score;
        assert verificationService.isPassing(score);
    }

    // 12. Secret Ownership Verification Invalid
    public static void test12_SecretVerificationInvalid() {
        FoundItem found = itemRepo.findFoundItemById(foundId).get();
        List<String> badAnswers = Arrays.asList("wrong answer", "no dent");

        double score = verificationService.evaluateAnswers(found, badAnswers);
        assert score == 0.0 : "Expected 0% verification, got " + score;
        assert !verificationService.isPassing(score);
    }

    // 13. Multiple Claim Prevention
    public static void test13_MultipleClaimPrevention() throws Exception {
        FoundItem found = itemRepo.findFoundItemById(foundId).get();

        // First claimant answers correctly
        Claim claim1 = claimService.submitClaim(found.getId(), "USR-1001",
                Arrays.asList("university crest", "small dent"));
        assert claim1.getStatus() == ClaimStatus.PENDING;

        // Second claimant attempts to claim the same item simultaneously
        boolean blocked = false;
        try {
            claimService.submitClaim(found.getId(), "USR-1003",
                    Arrays.asList("university crest", "small dent"));
        } catch (ClaimAlreadyExistsException e) {
            blocked = true;
        }
        assert blocked : "Simultaneous duplicate claim should be blocked!";
    }

    // 14. Administrator Claim Approval
    public static void test14_AdminClaimApproval() throws Exception {
        FoundItem found = itemRepo.findFoundItemById(foundId).get();
        Claim claim = claimRepo.findByFoundItemId(found.getId()).get(0);

        claimService.approveClaim(claim.getId(), "USR-1000", "Verified with student enrollment card.");
        Claim updated = claimRepo.findById(claim.getId()).get();
        assert updated.getStatus() == ClaimStatus.APPROVED;

        FoundItem updatedFound = itemRepo.findFoundItemById(found.getId()).get();
        assert updatedFound.getStatus() == ItemStatus.VERIFIED;
    }

    // 15. Item Return
    public static void test15_ItemReturn() throws Exception {
        FoundItem found = itemRepo.findFoundItemById(foundId).get();

        claimService.completeItemReturn(found.getId(), "USR-1000", "Handed over to Alice Sharma in person.");
        FoundItem updatedFound = itemRepo.findFoundItemById(found.getId()).get();
        assert updatedFound.getStatus() == ItemStatus.RETURNED;
    }

    // 16. Recovery Timeline
    public static void test16_RecoveryTimeline() {
        FoundItem found = itemRepo.findFoundItemById(foundId).get();
        List<RecoveryEvent> timeline = recoveryService.getTimelineForItem(found.getId());

        assert !timeline.isEmpty() : "Timeline should have events";
        boolean hasReported = timeline.stream().anyMatch(e -> "REPORTED".equals(e.getEventType()));
        boolean hasClaim = timeline.stream().anyMatch(e -> "CLAIM_SUBMITTED".equals(e.getEventType()));
        boolean hasApproved = timeline.stream().anyMatch(e -> "CLAIM_APPROVED".equals(e.getEventType()));
        boolean hasReturned = timeline.stream().anyMatch(e -> "ITEM_RETURNED".equals(e.getEventType()));

        assert hasReported && hasClaim && hasApproved && hasReturned : "Lifecycle stages missing in timeline";
    }

    // 17. Application Restart & Persistence
    public static void test17_PersistenceRestart() throws Exception {
        // Initialize fresh repository instances pointing to same storage
        UserRepository newUsers = new UserRepository(storageManager);
        ItemRepository newItems = new ItemRepository(storageManager);
        ClaimRepository newClaims = new ClaimRepository(storageManager);
        HistoryRepository newHistory = new HistoryRepository(storageManager);

        assert newUsers.findByUsername("tester1").isPresent() : "User not persisted";
        assert !newItems.getAllLostItems().isEmpty() : "Lost items not persisted";
        assert !newItems.getAllFoundItems().isEmpty() : "Found items not persisted";
        assert !newClaims.findAll().isEmpty() : "Claims not persisted";
        assert !newHistory.findAll().isEmpty() : "History not persisted";
    }

    // 18. Invalid Menu Input Validation
    public static void test18_InputValidation() {
        assert !com.intellirecover.util.InputValidator.isValidEmail("invalid-email");
        assert com.intellirecover.util.InputValidator.isValidEmail("alice@campus.edu");
        assert !com.intellirecover.util.InputValidator.isValidUsername("a"); // too short
        assert com.intellirecover.util.InputValidator.isValidUsername("valid_user99");
        assert !com.intellirecover.util.InputValidator.isValidPastOrPresentDate(LocalDate.now().plusDays(2));
    }

    // 19. Empty Search Results
    public static void test19_EmptySearchResults() {
        List<LostItem> none = itemService.searchLostItems("NonExistentKeywordXYZ123", null, null, null, null);
        assert none.isEmpty() : "Expected 0 results for non-existent keyword";
    }

    // 20. Malformed Data File Handling
    public static void test20_MalformedDataFileHandling() throws Exception {
        Path usersCsv = testDir.resolve("users.csv");
        // Append a corrupted line with mismatched columns
        Files.writeString(usersCsv, "CORRUPTED_ROW_WITHOUT_ENOUGH_COLUMNS\n",
                java.nio.file.StandardOpenOption.APPEND);

        // Reload should skip corrupted row without throwing or crashing
        UserRepository safeRepo = new UserRepository(storageManager);
        assert safeRepo.findByUsername("tester1").isPresent() : "Valid user should still load";
    }
}
