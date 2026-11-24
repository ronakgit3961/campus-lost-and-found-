package com.intellirecover.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe controlled ID generator for entities in IntelliRecover.
 * Automatically synchronizes with existing persisted records upon startup.
 */
public final class IDGenerator {
    private static final AtomicInteger USER_SEQ = new AtomicInteger(1000);
    private static final AtomicInteger LOST_SEQ = new AtomicInteger(1000);
    private static final AtomicInteger FOUND_SEQ = new AtomicInteger(1000);
    private static final AtomicInteger CLAIM_SEQ = new AtomicInteger(1000);
    private static final AtomicInteger EVENT_SEQ = new AtomicInteger(1000);
    private static final AtomicInteger NOTIF_SEQ = new AtomicInteger(1000);

    private IDGenerator() {
        // Prevent instantiation
    }

    public static String nextUserId() {
        return "USR-" + USER_SEQ.incrementAndGet();
    }

    public static String nextLostItemId() {
        return "LST-" + LOST_SEQ.incrementAndGet();
    }

    public static String nextFoundItemId() {
        return "FND-" + FOUND_SEQ.incrementAndGet();
    }

    public static String nextClaimId() {
        return "CLM-" + CLAIM_SEQ.incrementAndGet();
    }

    public static String nextEventId() {
        return "EVT-" + EVENT_SEQ.incrementAndGet();
    }

    public static String nextNotificationId() {
        return "NOTIF-" + NOTIF_SEQ.incrementAndGet();
    }

    /**
     * Updates sequence counters when loading existing items from files,
     * ensuring new IDs never conflict with existing IDs.
     */
    public static void synchronizeSequence(String id) {
        if (id == null) return;
        try {
            if (id.startsWith("USR-")) {
                int val = Integer.parseInt(id.substring(4));
                USER_SEQ.updateAndGet(cur -> Math.max(cur, val));
            } else if (id.startsWith("LST-")) {
                int val = Integer.parseInt(id.substring(4));
                LOST_SEQ.updateAndGet(cur -> Math.max(cur, val));
            } else if (id.startsWith("FND-")) {
                int val = Integer.parseInt(id.substring(4));
                FOUND_SEQ.updateAndGet(cur -> Math.max(cur, val));
            } else if (id.startsWith("CLM-")) {
                int val = Integer.parseInt(id.substring(4));
                CLAIM_SEQ.updateAndGet(cur -> Math.max(cur, val));
            } else if (id.startsWith("EVT-")) {
                int val = Integer.parseInt(id.substring(4));
                EVENT_SEQ.updateAndGet(cur -> Math.max(cur, val));
            } else if (id.startsWith("NOTIF-")) {
                int val = Integer.parseInt(id.substring(6));
                NOTIF_SEQ.updateAndGet(cur -> Math.max(cur, val));
            }
        } catch (NumberFormatException ignored) {
            // Non-standard ID, ignore for sequence tracking
        }
    }
}
