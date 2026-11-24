package com.intellirecover.service;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.RecoveryEvent;
import com.intellirecover.repository.HistoryRepository;
import com.intellirecover.util.DateUtil;
import com.intellirecover.util.IDGenerator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing audit events and building the chronological Recovery Timeline.
 */
public class RecoveryService {
    private final HistoryRepository historyRepository;

    public RecoveryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public synchronized void logEvent(String itemId, String claimId, String eventType,
                                      String actorUserId, String description) {
        try {
            String id = IDGenerator.nextEventId();
            RecoveryEvent event = new RecoveryEvent(id, itemId, claimId, eventType,
                    LocalDateTime.now(), actorUserId, description);
            historyRepository.save(event);
        } catch (StorageException e) {
            System.err.println("Failed to log recovery event: " + e.getMessage());
        }
    }

    public synchronized List<RecoveryEvent> getTimelineForItem(String itemId) {
        return historyRepository.findByItemId(itemId);
    }

    public synchronized List<RecoveryEvent> getAllEvents() {
        return historyRepository.findAll();
    }

    public String formatTimeline(String itemId) {
        List<RecoveryEvent> events = getTimelineForItem(itemId);
        if (events.isEmpty()) {
            return "No recorded history events for Item ID: " + itemId;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("                 RECOVERY TIMELINE: ").append(itemId).append("\n");
        sb.append("======================================================================\n");

        int step = 1;
        for (RecoveryEvent ev : events) {
            sb.append(String.format("%2d. [%s] %-22s (by %s)\n",
                    step++,
                    DateUtil.formatDateTime(ev.getTimestamp()),
                    ev.getEventType(),
                    ev.getActorUserId()));
            sb.append("    -> ").append(ev.getDescription()).append("\n");
        }
        sb.append("======================================================================\n");
        return sb.toString();
    }
}
