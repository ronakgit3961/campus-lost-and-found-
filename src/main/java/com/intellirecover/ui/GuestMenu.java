package com.intellirecover.ui;

import com.intellirecover.model.FoundItem;
import com.intellirecover.model.ItemCategory;
import com.intellirecover.service.ItemService;
import com.intellirecover.util.DateUtil;
import com.intellirecover.util.InputValidator;

import java.util.List;
import java.util.Scanner;

/**
 * Terminal UI controller for Guest users.
 * Provides public read-only access to found items without private verification details.
 */
public class GuestMenu {
    private final Scanner scanner;
    private final ItemService itemService;

    public GuestMenu(Scanner scanner, ItemService itemService) {
        this.scanner = scanner;
        this.itemService = itemService;
    }

    public void run() {
        boolean active = true;
        while (active) {
            System.out.println("\n==================================================");
            System.out.println("             GUEST PUBLIC PORTAL");
            System.out.println("  Browse found items on campus safely (Read-Only)");
            System.out.println("==================================================");
            System.out.println(" 1. Browse All Public Found Items");
            System.out.println(" 2. Search Found Items (Category, Location, Keyword)");
            System.out.println(" 3. How to Claim an Item (Public Guide)");
            System.out.println(" 4. Return to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter your choice (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleBrowsePublic();
                case "2" -> handleSearchPublic();
                case "3" -> handlePublicGuide();
                case "4" -> active = false;
                default -> System.out.println("\n[ERROR] Invalid choice. Select 1 to 4.");
            }
        }
    }

    private void handleBrowsePublic() {
        List<FoundItem> items = itemService.searchPublicFoundItems(null, null, null);
        System.out.println("\n==================================================");
        System.out.printf("  PUBLIC FOUND ITEMS DIRECTORY (%d items available)\n", items.size());
        System.out.println("==================================================");
        renderPublicItems(items);
    }

    private void handleSearchPublic() {
        System.out.println("\n--- SEARCH PUBLIC FOUND ITEMS ---");
        System.out.print("Keyword / Name (e.g. bottle, keys, calculator): ");
        String query = scanner.nextLine().trim();

        System.out.print("Filter by Category? (y/n): ");
        ItemCategory category = null;
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            ItemCategory[] cats = ItemCategory.values();
            for (int i = 0; i < cats.length; i++) {
                System.out.printf(" [%d] %s\n", i + 1, cats[i].getDisplayName());
            }
            System.out.print("Select category number: ");
            int sel = InputValidator.parsePositiveInt(scanner.nextLine().trim(), -1);
            if (sel >= 1 && sel <= cats.length) {
                category = cats[sel - 1];
            }
        }

        System.out.print("Campus Location keyword (e.g. Library, Cafeteria): ");
        String location = scanner.nextLine().trim();

        List<FoundItem> items = itemService.searchPublicFoundItems(query, category, location);
        System.out.printf("\n--- SEARCH RESULTS (%d matches) ---\n", items.size());
        renderPublicItems(items);
    }

    private void handlePublicGuide() {
        System.out.println("\n==================================================");
        System.out.println("       HOW TO RECOVER YOUR ITEM VIA INTELLIRECOVER ");
        System.out.println("==================================================");
        System.out.println(" 1. Register or Log in as a University Student.");
        System.out.println(" 2. Report your lost item with as much detail as possible.");
        System.out.println(" 3. The Smart Match Engine will calculate matching probability.");
        System.out.println(" 4. Submit an ownership claim by answering the private verification questions.");
        System.out.println(" 5. Campus Administrators will verify and approve your claim.");
        System.out.println(" 6. Visit the designated campus custody desk to retrieve your belonging.");
        System.out.println("==================================================");
    }

    private void renderPublicItems(List<FoundItem> items) {
        if (items.isEmpty()) {
            System.out.println(" No public found items found.");
            return;
        }

        System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-20s\n",
                "ID", "NAME", "CATEGORY", "LOCATION FOUND", "DATE FOUND", "STORAGE CUSTODY");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        for (FoundItem item : items) {
            String name = item.getName().length() > 22 ? item.getName().substring(0, 19) + "..." : item.getName();
            String cat = item.getCategory().getDisplayName().length() > 18 ? item.getCategory().getDisplayName().substring(0, 15) + "..." : item.getCategory().getDisplayName();
            String loc = item.getLocation().length() > 20 ? item.getLocation().substring(0, 17) + "..." : item.getLocation();
            String custody = item.getStorageLocation().length() > 20 ? item.getStorageLocation().substring(0, 17) + "..." : item.getStorageLocation();
            System.out.printf("%-10s | %-22s | %-18s | %-20s | %-12s | %-20s\n",
                    item.getId(), name, cat, loc, DateUtil.formatDate(item.getDateFound()), custody);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.println("Note: Secret identifying details are concealed to protect true ownership.");
    }
}
