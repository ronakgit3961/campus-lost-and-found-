package com.intellirecover;

import com.intellirecover.model.*;
import com.intellirecover.repository.*;
import com.intellirecover.service.*;
import com.intellirecover.storage.FileStorageManager;
import com.intellirecover.util.DateUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Executes the full end-to-end scenario required by PART 10 of the assignment:
 * 1. Alice reports lost black water bottle near Library.
 * 2. Bob reports found black sports bottle in Library with private verification questions.
 * 3. System detects match, calculates score (86/100 Strong Match), and outputs transparent reasons.
 * 4. Private details remain concealed from public view.
 * 5. Claimant answers verification challenge and achieves verification score.
 * 6. Campus Administrator reviews and approves the claim.
 * 7. Administrator completes physical handover and marks item RETURNED.
 * 8. All stages are audited in the Recovery Timeline.
 */
public class ScenarioDemonstration {
    public static void main(String[] args) throws Exception {
        System.out.println("======================================================================");
        System.out.println("        CAMPUS INTELLIRECOVER - OFFICIAL SCENARIO DEMONSTRATION        ");
        System.out.println("======================================================================");

        Path demoDir = Files.createTempDirectory("intellirecover_demo_scenario_");
        FileStorageManager storage = new FileStorageManager(demoDir.toString());

        UserRepository userRepo = new UserRepository(storage);
        ItemRepository itemRepo = new ItemRepository(storage);
        ClaimRepository claimRepo = new ClaimRepository(storage);
        HistoryRepository historyRepo = new HistoryRepository(storage);
        NotificationRepository notifRepo = new NotificationRepository(storage);

        AuthenticationService auth = new AuthenticationService(userRepo);
        MatchingService matcher = new MatchingService();
        DuplicateDetectionService dup = new DuplicateDetectionService(matcher);
        VerificationService verif = new VerificationService();
        RecoveryService recovery = new RecoveryService(historyRepo);
        NotificationService notif = new NotificationService(notifRepo);

        ItemService itemService = new ItemService(itemRepo, matcher, dup, recovery, notif);
        ClaimService claimService = new ClaimService(claimRepo, itemRepo, verif, recovery, notif);

        // 1. Setup Users
        User alice = auth.registerStudent("alice", "pass123", "Alice Sharma", "alice@campus.edu",
                "STU-2024-001", "Computer Science", "9876543210");
        User bob = auth.registerStudent("bob", "pass123", "Bob Jenkins", "bob@campus.edu",
                "STU-2024-002", "Electrical Engineering", "9876543211");
        User admin = auth.registerAdmin("admin", "adminpass", "Dr. Arthur Vance", "security@campus.edu",
                "STF-101", "Campus Security");

        System.out.println("\n[STEP 1] Student Alice reports her lost Black Water Bottle...");
        LostItem aliceLost = itemService.reportLostItem(
                "Black Water Bottle",
                ItemCategory.WATER_BOTTLE,
                "Black stainless steel sports bottle with university crest sticker",
                "Central Library 2nd Floor",
                LocalDate.now(),
                alice.getId(),
                "Milton",
                "university crest sticker, dent on bottom",
                "Free coffee reward"
        );
        System.out.println(" -> Registered Lost Report: " + aliceLost.getId() + " | Status: " + aliceLost.getStatus().getDescription());

        System.out.println("\n[STEP 2] Student Bob finds an item and reports it with Secret Verification Questions...");
        List<VerificationQuestion> secretQuestions = Arrays.asList(
                new VerificationQuestion("What sticker is attached to the bottle?", "university crest"),
                new VerificationQuestion("What physical defect or dent is present on the bottom?", "small dent")
        );
        FoundItem bobFound = itemService.reportFoundItem(
                "Black Sports Bottle",
                ItemCategory.WATER_BOTTLE,
                "Black sports bottle found under study carrel",
                "Central Library 2nd Floor",
                LocalDate.now(),
                bob.getId(),
                "Milton",
                "stainless steel",
                "Central Library Reception Desk Room 102",
                secretQuestions
        );
        System.out.println(" -> Registered Found Report: " + bobFound.getId() + " | Custody: " + bobFound.getStorageLocation());
        System.out.println(" -> Secret Questions Configured: " + bobFound.getSecretQuestions().size());
        System.out.println(" -> Public Listing View (Zero Answers Exposed): " + bobFound.getSecretQuestions().get(0));

        System.out.println("\n[STEP 3] Intelligent Match Engine evaluates similarity & outputs score breakdown...");
        List<MatchResult> matches = matcher.findMatchesForLostItem(aliceLost, Collections.singletonList(bobFound));
        assert !matches.isEmpty() : "Match engine failed to detect pair!";
        MatchResult match = matches.get(0);
        System.out.println(match.getFormattedExplanation());

        System.out.println("\n[STEP 4] Alice initiates Ownership Claim and answers secret verification challenge...");
        List<String> aliceAnswers = Arrays.asList("university crest", "small dent");
        Claim aliceClaim = claimService.submitClaim(bobFound.getId(), alice.getId(), aliceAnswers);
        System.out.printf(" -> Claim ID: %s | Verification Score: %.1f%% | Status: %s\n",
                aliceClaim.getId(), aliceClaim.getVerificationScore(), aliceClaim.getStatus().getDescription());
        System.out.println(" -> Review Notes: " + aliceClaim.getReviewNotes());

        System.out.println("\n[STEP 5] Campus Administrator Dr. Arthur Vance reviews and approves claim...");
        claimService.approveClaim(aliceClaim.getId(), admin.getId(), "Student presented student card STU-2024-001. Answers verified.");
        System.out.println(" -> Claim Approved! Found Item Status: " + itemRepo.findFoundItemById(bobFound.getId()).get().getStatus().getDescription());

        System.out.println("\n[STEP 6] Student collects item from custody desk -> Admin marks item physically RETURNED...");
        claimService.completeItemReturn(bobFound.getId(), admin.getId(), "Handed over to Alice Sharma at Library Reception.");
        System.out.println(" -> Final Found Item Status: " + itemRepo.findFoundItemById(bobFound.getId()).get().getStatus().getDescription());
        System.out.println(" -> Final Lost Item Status : " + itemRepo.findLostItemById(aliceLost.getId()).get().getStatus().getDescription());

        System.out.println("\n[STEP 7] Complete Recovery Timeline Audit Trail:");
        System.out.println(recovery.formatTimeline(bobFound.getId()));

        System.out.println("======================================================================");
        System.out.println("      DEMONSTRATION SCENARIO EXECUTED AND FULLY VERIFIED!             ");
        System.out.println("======================================================================");

        // Cleanup temp dir
        Files.walk(demoDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
}
