package com.intellirecover.storage;

import com.intellirecover.model.*;
import com.intellirecover.repository.*;
import com.intellirecover.service.MatchingService;
import com.intellirecover.util.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Initializes realistic demonstration datasets reflecting campus life.
 * Sets up students, admin, lost and found items, pending/approved claims,
 * and recovery history without destructive overwrite of existing real data.
 */
public class DemoDataInitializer {

    public static boolean seedDemoDataIfEmpty(UserRepository userRepo,
                                              ItemRepository itemRepo,
                                              ClaimRepository claimRepo,
                                              HistoryRepository historyRepo,
                                              NotificationRepository notifRepo,
                                              MatchingService matchingService) {

        if (!userRepo.findAll().isEmpty()) {
            // Already has data; do not overwrite
            return false;
        }

        try {
            System.out.println("[DEMO SEED] Populating initial campus demonstration data...");

            // 1. Users (5 Students, 1 Admin)
            String salt1 = PasswordUtil.generateSalt();
            Student alice = new Student("USR-1001", "alice", PasswordUtil.hashPassword("password123", salt1), salt1,
                    "Alice Sharma", "alice@campus.edu", LocalDateTime.now().minusDays(30),
                    "STU-2024-001", "Computer Science", "9876543210");

            String salt2 = PasswordUtil.generateSalt();
            Student bob = new Student("USR-1002", "bob", PasswordUtil.hashPassword("password123", salt2), salt2,
                    "Bob Jenkins", "bob@campus.edu", LocalDateTime.now().minusDays(28),
                    "STU-2024-002", "Electrical Engineering", "9876543211");

            String salt3 = PasswordUtil.generateSalt();
            Student charlie = new Student("USR-1003", "charlie", PasswordUtil.hashPassword("password123", salt3), salt3,
                    "Charlie Davis", "charlie@campus.edu", LocalDateTime.now().minusDays(25),
                    "STU-2024-003", "Mechanical Engineering", "9876543212");

            String salt4 = PasswordUtil.generateSalt();
            Student diana = new Student("USR-1004", "diana", PasswordUtil.hashPassword("password123", salt4), salt4,
                    "Diana Prince", "diana@campus.edu", LocalDateTime.now().minusDays(20),
                    "STU-2024-004", "Business Administration", "9876543213");

            String salt5 = PasswordUtil.generateSalt();
            Student eva = new Student("USR-1005", "eva", PasswordUtil.hashPassword("password123", salt5), salt5,
                    "Eva Green", "eva@campus.edu", LocalDateTime.now().minusDays(15),
                    "STU-2024-005", "Biotechnology", "9876543214");

            String saltAdmin = PasswordUtil.generateSalt();
            Administrator admin = new Administrator("USR-1000", "admin", PasswordUtil.hashPassword("adminpass", saltAdmin), saltAdmin,
                    "Dr. Arthur Vance", "security.head@campus.edu", LocalDateTime.now().minusDays(60),
                    "STF-101", "Campus Security & Student Affairs");

            userRepo.save(admin);
            userRepo.save(alice);
            userRepo.save(bob);
            userRepo.save(charlie);
            userRepo.save(diana);
            userRepo.save(eva);

            // 2. Lost Items (10 items)
            LocalDate today = LocalDate.now();

            // LST-1001: Alice's Black Water Bottle (Matching Scenario)
            LostItem l1 = createLostItem("LST-1001", "Black Water Bottle", ItemCategory.WATER_BOTTLE,
                    "Black stainless steel insulated bottle 750ml with university crest sticker",
                    "Central Library 2nd Floor", today.minusDays(2), ItemStatus.MATCH_PENDING,
                    "USR-1001", "Milton", "university crest sticker, small bottom dent", today.minusDays(2), "Free coffee reward", matchingService);

            // LST-1002: Bob's Scientific Calculator
            LostItem l2 = createLostItem("LST-1002", "Casio Scientific Calculator", ItemCategory.ELECTRONICS,
                    "Casio fx-991EX ClassWiz calculator with blue sticker initials BJ on back cover",
                    "Engineering Block Room 204", today.minusDays(5), ItemStatus.REPORTED,
                    "USR-1002", "Casio", "blue sticker with BJ initials", today.minusDays(5), "", matchingService);

            // LST-1003: Charlie's Leather Wallet (Will have verified/approved claim)
            LostItem l3 = createLostItem("LST-1003", "Brown Leather Wallet", ItemCategory.BAGS_WALLETS,
                    "Brown genuine leather bifold wallet containing student ID card and bus pass",
                    "Campus Cafeteria", today.minusDays(10), ItemStatus.RETURNED,
                    "USR-1003", "Wildhorn", "student ID card for Charlie Davis", today.minusDays(10), "", matchingService);

            // LST-1004: Diana's Apple AirPods Pro
            LostItem l4 = createLostItem("LST-1004", "Apple AirPods Pro", ItemCategory.ELECTRONICS,
                    "White Apple AirPods Pro 2nd Gen in black silicone protective case with carabiner",
                    "Sports Complex Gym", today.minusDays(15), ItemStatus.REPORTED,
                    "USR-1004", "Apple", "black silicone protective case", today.minusDays(15), "500 reward", matchingService);

            // LST-1005: Eva's Biochemistry Textbook
            LostItem l5 = createLostItem("LST-1005", "Biochemistry Textbook", ItemCategory.BOOKS_STATIONERY,
                    "Lehninger Principles of Biochemistry 8th Edition with yellow highlighter notes",
                    "Science Block Lab 3", today.minusDays(8), ItemStatus.REPORTED,
                    "USR-1005", "Macmillan", "yellow highlighter marks on chapter 4", today.minusDays(8), "", matchingService);

            // LST-1006: Room Keys
            LostItem l6 = createLostItem("LST-1006", "Hostel Room Keys on Lanyard", ItemCategory.KEYS,
                    "Set of three Yale brass keys attached to blue university sports lanyard",
                    "Parking Lot North", today.minusDays(12), ItemStatus.REPORTED,
                    "USR-1001", "Yale", "blue university sports lanyard", today.minusDays(12), "", matchingService);

            // LST-1007: Denim Jacket (Already returned)
            LostItem l7 = createLostItem("LST-1007", "Denim Jacket Blue", ItemCategory.CLOTHING,
                    "Blue denim trucker jacket size M with brass buttons",
                    "Main Auditorium Row G", today.minusDays(20), ItemStatus.RETURNED,
                    "USR-1002", "Levis", "size M, interior tag initialed", today.minusDays(20), "", matchingService);

            // LST-1008: Wristwatch
            LostItem l8 = createLostItem("LST-1008", "Titan Analog Wristwatch", ItemCategory.ACCESSORIES,
                    "Titan silver metallic chain wristwatch with black round dial",
                    "Science Block Room 101", today.minusDays(25), ItemStatus.REPORTED,
                    "USR-1003", "Titan", "black dial silver mesh strap", today.minusDays(25), "", matchingService);

            // LST-1009: Umbrella (Long lost)
            LostItem l9 = createLostItem("LST-1009", "Black Folding Umbrella", ItemCategory.OTHER,
                    "Automatic compact black umbrella with curved wooden handle",
                    "Central Library Entrance", today.minusDays(40), ItemStatus.REPORTED,
                    "USR-1004", "SunShield", "wooden curved handle", today.minusDays(40), "", matchingService);

            // LST-1010: USB-C Laptop Charger
            LostItem l10 = createLostItem("LST-1010", "Lenovo 65W USB-C Charger", ItemCategory.ELECTRONICS,
                    "Black 65W Type-C AC power adapter with power brick",
                    "Campus Cafeteria Charging Counter", today.minusDays(50), ItemStatus.REPORTED,
                    "USR-1005", "Lenovo", "small red tape on wire", today.minusDays(50), "", matchingService);

            itemRepo.saveLostItem(l1);
            itemRepo.saveLostItem(l2);
            itemRepo.saveLostItem(l3);
            itemRepo.saveLostItem(l4);
            itemRepo.saveLostItem(l5);
            itemRepo.saveLostItem(l6);
            itemRepo.saveLostItem(l7);
            itemRepo.saveLostItem(l8);
            itemRepo.saveLostItem(l9);
            itemRepo.saveLostItem(l10);

            // 3. Found Items (10 items)
            // FND-1001: Bob's Found Black Sports Bottle (Matches LST-1001)
            List<VerificationQuestion> q1 = Arrays.asList(
                    new VerificationQuestion("What sticker is attached to the bottle?", "university crest"),
                    new VerificationQuestion("What physical defect or dent is present on the bottom?", "small dent")
            );
            FoundItem f1 = createFoundItem("FND-1001", "Black Sports Bottle", ItemCategory.WATER_BOTTLE,
                    "Black stainless steel sports bottle found under study carrel desk",
                    "Central Library 2nd Floor", today.minusDays(2), ItemStatus.MATCH_PENDING,
                    "USR-1002", "Milton", "university crest sticker, dent on bottom", today.minusDays(2),
                    "Library Circulation Counter Desk", q1, null, matchingService);

            // FND-1002: Casio Calculator Found
            List<VerificationQuestion> q2 = Arrays.asList(
                    new VerificationQuestion("What initials are written on the back sticker?", "BJ")
            );
            FoundItem f2 = createFoundItem("FND-1002", "Casio Scientific Calculator", ItemCategory.ELECTRONICS,
                    "Found Casio fx-991EX calculator left on bench near Room 204",
                    "Engineering Block Corridor", today.minusDays(4), ItemStatus.REPORTED,
                    "USR-1003", "Casio", "blue sticker on back", today.minusDays(4),
                    "Engineering Dept Office Room 101", q2, null, matchingService);

            // FND-1003: Brown Leather Wallet (Claimed and Returned to Charlie)
            List<VerificationQuestion> q3 = Arrays.asList(
                    new VerificationQuestion("What student name is on the ID card inside?", "Charlie Davis")
            );
            FoundItem f3 = createFoundItem("FND-1003", "Brown Bifold Leather Wallet", ItemCategory.BAGS_WALLETS,
                    "Brown leather wallet found near tray drop-off station",
                    "Campus Cafeteria", today.minusDays(9), ItemStatus.RETURNED,
                    "USR-1004", "Wildhorn", "contains student ID", today.minusDays(9),
                    "Security Office Central Desk", q3, "CLM-1001", matchingService);

            // FND-1004: Wireless Earbuds Found
            List<VerificationQuestion> q4 = Arrays.asList(
                    new VerificationQuestion("What color is the silicone outer cover?", "black")
            );
            FoundItem f4 = createFoundItem("FND-1004", "Wireless Earbuds in Case", ItemCategory.ELECTRONICS,
                    "White wireless earbuds in a silicone case found near locker benches",
                    "Sports Complex Gym", today.minusDays(14), ItemStatus.REPORTED,
                    "USR-1005", "Apple", "black silicone case", today.minusDays(14),
                    "Sports Complex Reception", q4, null, matchingService);

            // FND-1005: Biochemistry Textbook Found
            FoundItem f5 = createFoundItem("FND-1005", "Biochemistry Hardcover Book", ItemCategory.BOOKS_STATIONERY,
                    "Thick biochemistry textbook left at lab station",
                    "Science Block Lab 3", today.minusDays(7), ItemStatus.REPORTED,
                    "USR-1001", "Macmillan", "highlighted notes", today.minusDays(7),
                    "Science Lab Assistant Room", Collections.emptyList(), null, matchingService);

            // FND-1006: Keychain Found
            FoundItem f6 = createFoundItem("FND-1006", "Set of Keys with Lanyard", ItemCategory.KEYS,
                    "Three keys on blue lanyard found on walkway near bicycle stand",
                    "Parking Lot North", today.minusDays(11), ItemStatus.REPORTED,
                    "USR-1002", "Yale", "blue sports lanyard", today.minusDays(11),
                    "Security Gate 1", Collections.emptyList(), null, matchingService);

            // FND-1007: Denim Jacket (Already Returned)
            FoundItem f7 = createFoundItem("FND-1007", "Blue Denim Trucker Jacket", ItemCategory.CLOTHING,
                    "Denim jacket recovered from auditorium seat row G",
                    "Main Auditorium", today.minusDays(20), ItemStatus.RETURNED,
                    "USR-1000", "Levis", "size M", today.minusDays(20),
                    "Campus Lost & Found Vault", Collections.emptyList(), "CLM-1002", matchingService);

            // FND-1008: Wristwatch Found (Pending Claim by Charlie)
            List<VerificationQuestion> q8 = Arrays.asList(
                    new VerificationQuestion("What color is the watch dial?", "black")
            );
            FoundItem f8 = createFoundItem("FND-1008", "Silver Chain Analog Watch", ItemCategory.ACCESSORIES,
                    "Silver mesh wrist watch discovered on lecturer podium",
                    "Science Block Room 101", today.minusDays(23), ItemStatus.CLAIM_PENDING,
                    "USR-1004", "Titan", "silver chain black dial", today.minusDays(23),
                    "Security Office Central Desk", q8, "CLM-1003", matchingService);

            // FND-1009: Black Umbrella (Unclaimed >40 days -> Rescue candidate!)
            FoundItem f9 = createFoundItem("FND-1009", "Black Folding Umbrella", ItemCategory.OTHER,
                    "Black portable umbrella left in umbrella stand during heavy rain",
                    "Central Library Entrance", today.minusDays(45), ItemStatus.REPORTED,
                    "USR-1005", "SunShield", "wooden curved handle", today.minusDays(45),
                    "Library Storage Shelf B", Collections.emptyList(), null, matchingService);

            // FND-1010: USB-C Adapter (Unclaimed >50 days -> Rescue candidate!)
            FoundItem f10 = createFoundItem("FND-1010", "Lenovo 65W Laptop Charger", ItemCategory.ELECTRONICS,
                    "Type-C laptop charging brick left plugged into cafeteria wall outlet",
                    "Campus Cafeteria", today.minusDays(52), ItemStatus.REPORTED,
                    "USR-1001", "Lenovo", "red electrical tape", today.minusDays(52),
                    "Security Office Locker 4", Collections.emptyList(), null, matchingService);

            itemRepo.saveFoundItem(f1);
            itemRepo.saveFoundItem(f2);
            itemRepo.saveFoundItem(f3);
            itemRepo.saveFoundItem(f4);
            itemRepo.saveFoundItem(f5);
            itemRepo.saveFoundItem(f6);
            itemRepo.saveFoundItem(f7);
            itemRepo.saveFoundItem(f8);
            itemRepo.saveFoundItem(f9);
            itemRepo.saveFoundItem(f10);

            // 4. Claims (Completed recovery for Charlie, Pending claim for Charlie's watch)
            Claim c1 = new Claim("CLM-1001", "FND-1003", "USR-1003",
                    LocalDateTime.now().minusDays(8), Arrays.asList("Charlie Davis"),
                    100.0, ClaimStatus.APPROVED, "Verified identity via university roll card.",
                    "USR-1000", LocalDateTime.now().minusDays(8));

            Claim c2 = new Claim("CLM-1002", "FND-1007", "USR-1002",
                    LocalDateTime.now().minusDays(19), Arrays.asList("Bob Jenkins initials on label"),
                    100.0, ClaimStatus.APPROVED, "Verified size M and initials.",
                    "USR-1000", LocalDateTime.now().minusDays(18));

            Claim c3 = new Claim("CLM-1003", "FND-1008", "USR-1003",
                    LocalDateTime.now().minusDays(2), Arrays.asList("black"),
                    100.0, ClaimStatus.PENDING, "Awaiting physical verification of watch dial.",
                    null, null);

            claimRepo.save(c1);
            claimRepo.save(c2);
            claimRepo.save(c3);

            // 5. Recovery Timeline Events
            historyRepo.save(new RecoveryEvent("EVT-1001", "FND-1003", null, "REPORTED",
                    LocalDateTime.now().minusDays(9), "USR-1004", "Found brown wallet reported by Diana Prince."));
            historyRepo.save(new RecoveryEvent("EVT-1002", "FND-1003", null, "MATCH_DETECTED",
                    LocalDateTime.now().minusDays(9), "SYSTEM", "Automatic match detected with Lost Item LST-1003."));
            historyRepo.save(new RecoveryEvent("EVT-1003", "FND-1003", "CLM-1001", "CLAIM_SUBMITTED",
                    LocalDateTime.now().minusDays(8), "USR-1003", "Charlie Davis answered verification questions (Score 100%)."));
            historyRepo.save(new RecoveryEvent("EVT-1004", "FND-1003", "CLM-1001", "CLAIM_APPROVED",
                    LocalDateTime.now().minusDays(8), "USR-1000", "Administrator Dr. Arthur Vance approved claim."));
            historyRepo.save(new RecoveryEvent("EVT-1005", "FND-1003", "CLM-1001", "ITEM_RETURNED",
                    LocalDateTime.now().minusDays(7), "USR-1000", "Wallet handed over in person to Charlie Davis."));
            historyRepo.save(new RecoveryEvent("EVT-1006", "FND-1003", "CLM-1001", "RECOVERY_COMPLETED",
                    LocalDateTime.now().minusDays(7), "USR-1000", "Recovery lifecycle closed successfully."));

            historyRepo.save(new RecoveryEvent("EVT-1007", "FND-1001", null, "REPORTED",
                    LocalDateTime.now().minusDays(2), "USR-1002", "Bob reported finding black sports bottle in Library."));
            historyRepo.save(new RecoveryEvent("EVT-1008", "LST-1001", null, "REPORTED",
                    LocalDateTime.now().minusDays(2), "USR-1001", "Alice reported lost black water bottle in Library."));
            historyRepo.save(new RecoveryEvent("EVT-1009", "LST-1001", null, "MATCH_DETECTED",
                    LocalDateTime.now().minusDays(2), "SYSTEM", "IntelliRecover calculated Strong Match (86/100) with FND-1001."));

            // 6. Notifications
            notifRepo.save(new Notification("NOTIF-1001", "USR-1001", "High Match Found!",
                    "A found item 'Black Sports Bottle' (FND-1001) in Central Library matches your report (Score: 86/100).",
                    LocalDateTime.now().minusDays(2), false));
            notifRepo.save(new Notification("NOTIF-1002", "USR-1003", "Wallet Recovered",
                    "Your wallet claim CLM-1001 was approved and handed over.",
                    LocalDateTime.now().minusDays(7), true));

            System.out.println("[DEMO SEED] Successfully seeded 6 users, 20 items, 3 claims, and recovery timeline.");
            return true;

        } catch (Exception e) {
            System.err.println("[DEMO SEED] Error seeding demo data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static LostItem createLostItem(String id, String name, ItemCategory cat, String desc,
                                           String loc, LocalDate reported, ItemStatus status,
                                           String reporter, String brand, String attrs,
                                           LocalDate lostDate, String reward, MatchingService matchingService) {
        Set<String> kw = matchingService.extractKeywords(name + " " + desc);
        String fp = FingerprintUtil.generateFingerprint(cat.name(), name, brand, kw, attrs);
        return new LostItem(id, name, cat, desc, loc, reported, status, reporter, fp, brand, attrs, lostDate, reward);
    }

    private static FoundItem createFoundItem(String id, String name, ItemCategory cat, String desc,
                                             String loc, LocalDate reported, ItemStatus status,
                                             String reporter, String brand, String attrs,
                                             LocalDate foundDate, String storage,
                                             List<VerificationQuestion> questions, String claimId,
                                             MatchingService matchingService) {
        Set<String> kw = matchingService.extractKeywords(name + " " + desc);
        String fp = FingerprintUtil.generateFingerprint(cat.name(), name, brand, kw, attrs);
        return new FoundItem(id, name, cat, desc, loc, reported, status, reporter, fp, brand, attrs,
                foundDate, storage, questions, claimId);
    }
}
