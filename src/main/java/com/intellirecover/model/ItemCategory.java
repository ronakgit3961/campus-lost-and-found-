package com.intellirecover.model;

/**
 * Standard categories for lost and found campus belongings.
 */
public enum ItemCategory {
    ELECTRONICS("Electronics & Gadgets"),
    DOCUMENTS_ID("Cards & Official Documents"),
    BOOKS_STATIONERY("Books & Study Materials"),
    ACCESSORIES("Watches, Jewellery & Accessories"),
    BAGS_WALLETS("Bags, Backpacks & Wallets"),
    KEYS("Keys & Keychains"),
    WATER_BOTTLE("Water Bottles & Flasks"),
    CLOTHING("Clothing & Wearables"),
    OTHER("Miscellaneous & Other");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ItemCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        for (ItemCategory cat : ItemCategory.values()) {
            if (cat.name().equalsIgnoreCase(text.trim()) ||
                cat.displayName.equalsIgnoreCase(text.trim())) {
                return cat;
            }
        }
        return OTHER;
    }
}
