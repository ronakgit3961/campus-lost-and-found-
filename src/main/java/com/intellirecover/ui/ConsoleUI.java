package com.intellirecover.ui;

import com.intellirecover.exception.AuthenticationException;
import com.intellirecover.model.User;
import com.intellirecover.model.UserRole;
import com.intellirecover.repository.*;
import com.intellirecover.service.*;
import com.intellirecover.storage.DemoDataInitializer;
import com.intellirecover.storage.FileStorageManager;

import java.util.Scanner;

/**
 * Main terminal interface controller for IntelliRecover.
 * Handles the top-level application loop, authentication, and routing.
 */
public class ConsoleUI {
    private final Scanner scanner;
    private final FileStorageManager storageManager;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final HistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    private final AuthenticationService authService;
    private final MatchingService matchingService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final VerificationService verificationService;
    private final RecoveryService recoveryService;
    private final NotificationService notificationService;
    private final ItemService itemService;
    private final ClaimService claimService;
    private final AnalyticsService analyticsService;
    private final RescueService rescueService;

    private final StudentMenu studentMenu;
    private final AdminMenu adminMenu;
    private final GuestMenu guestMenu;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.storageManager = new FileStorageManager("data");

        // Repositories
        this.userRepository = new UserRepository(storageManager);
        this.itemRepository = new ItemRepository(storageManager);
        this.claimRepository = new ClaimRepository(storageManager);
        this.historyRepository = new HistoryRepository(storageManager);
        this.notificationRepository = new NotificationRepository(storageManager);

        // Services
        this.authService = new AuthenticationService(userRepository);
        this.matchingService = new MatchingService();
        this.duplicateDetectionService = new DuplicateDetectionService(matchingService);
        this.verificationService = new VerificationService();
        this.recoveryService = new RecoveryService(historyRepository);
        this.notificationService = new NotificationService(notificationRepository);

        this.itemService = new ItemService(itemRepository, matchingService,
                duplicateDetectionService, recoveryService, notificationService);

        this.claimService = new ClaimService(claimRepository, itemRepository,
                verificationService, recoveryService, notificationService);

        this.analyticsService = new AnalyticsService(itemRepository, claimRepository);
        this.rescueService = new RescueService(itemRepository);

        // Submenus
        this.studentMenu = new StudentMenu(scanner, authService, itemService,
                matchingService, claimService, recoveryService, notificationService);

        this.adminMenu = new AdminMenu(scanner, authService, itemService, claimService,
                analyticsService, rescueService, recoveryService, duplicateDetectionService);

        this.guestMenu = new GuestMenu(scanner, itemService);

        // Automatically seed demo data if repository is empty
        DemoDataInitializer.seedDemoDataIfEmpty(userRepository, itemRepository,
                claimRepository, historyRepository, notificationRepository, matchingService);
    }

    public void start() {
        printBanner();
        boolean running = true;

        while (running) {
            printMainMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> handleStudentLogin();
                case "2" -> handleStudentRegistration();
                case "3" -> handleAdminLogin();
                case "4" -> guestMenu.run();
                case "5" -> handleResetDemoData();
                case "6" -> {
                    System.out.println("\nThank you for using Campus Lost & Found – IntelliRecover. Goodbye!");
                    running = false;
                }
                default -> System.out.println("\n[ERROR] Invalid option. Please select 1 through 6.");
            }
        }
    }

    private void printBanner() {
        System.out.println("==================================================");
        System.out.println("          CAMPUS INTELLIRECOVER                   ");
        System.out.println("       SMART LOST & FOUND SYSTEM                  ");
        System.out.println("   Intelligent Item Recovery & Ownership Engine   ");
        System.out.println("==================================================");
    }

    private void printMainMenu() {
        System.out.println("\n==================================================");
        System.out.println("                   MAIN MENU                      ");
        System.out.println("==================================================");
        System.out.println("1. Student Login");
        System.out.println("2. Student Registration");
        System.out.println("3. Administrator Login");
        System.out.println("4. Continue as Guest");
        System.out.println("5. Reload Demonstration Dataset");
        System.out.println("6. Exit");
        System.out.println("==================================================");
    }

    private void handleStudentLogin() {
        System.out.println("\n--- STUDENT LOGIN ---");
        System.out.print("Username (e.g. alice, bob, charlie): ");
        String username = scanner.nextLine().trim();

        System.out.print("Password (demo: password123): ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.login(username, password);
            if (user.getRole() != UserRole.STUDENT) {
                authService.logout();
                System.out.println("[ERROR] This account is an Administrator. Please use option 3 (Administrator Login).");
                return;
            }
            System.out.printf("\n[SUCCESS] Welcome back, %s!\n", user.getFullName());
            studentMenu.run();
        } catch (AuthenticationException e) {
            System.out.println("\n[ERROR] Authentication failed: " + e.getMessage());
        }
    }

    private void handleStudentRegistration() {
        System.out.println("\n--- STUDENT REGISTRATION ---");
        System.out.print("Desired Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password (min 4 characters): ");
        String password = scanner.nextLine().trim();

        System.out.print("Full Legal Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("University Email Address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Student Enrollment ID (e.g. STU-2024-099): ");
        String studentId = scanner.nextLine().trim();

        System.out.print("Department / Major: ");
        String department = scanner.nextLine().trim();

        System.out.print("Contact Phone Number: ");
        String phone = scanner.nextLine().trim();

        try {
            User user = authService.registerStudent(username, password, fullName, email,
                    studentId, department, phone);
            System.out.printf("\n[SUCCESS] Registration complete! Account created for %s (ID: %s).\n",
                    user.getFullName(), user.getId());
            studentMenu.run();
        } catch (Exception e) {
            System.out.println("\n[ERROR] Registration failed: " + e.getMessage());
        }
    }

    private void handleAdminLogin() {
        System.out.println("\n--- ADMINISTRATOR LOGIN ---");
        System.out.print("Admin Username (demo: admin): ");
        String username = scanner.nextLine().trim();

        System.out.print("Password (demo: adminpass): ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.login(username, password);
            if (user.getRole() != UserRole.ADMINISTRATOR) {
                authService.logout();
                System.out.println("[ERROR] This account is a Student. Please use option 1 (Student Login).");
                return;
            }
            System.out.printf("\n[SUCCESS] Administrator session initiated for %s.\n", user.getFullName());
            adminMenu.run();
        } catch (AuthenticationException e) {
            System.out.println("\n[ERROR] Administrator login failed: " + e.getMessage());
        }
    }

    private void handleResetDemoData() {
        System.out.print("\nAre you sure you want to reload the official demonstration dataset? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            userRepository.findAll().clear(); // allow seeder to re-populate
            DemoDataInitializer.seedDemoDataIfEmpty(userRepository, itemRepository,
                    claimRepository, historyRepository, notificationRepository, matchingService);
            System.out.println("[SUCCESS] Demonstration data refreshed.");
        } else {
            System.out.println("[INFO] Reset cancelled.");
        }
    }
}
