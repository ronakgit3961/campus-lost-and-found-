package com.intellirecover.model;

import java.time.LocalDateTime;

/**
 * Represents an immutable audit trail event in the lifecycle of an item.
 * Used to construct the chronological Recovery Timeline.
 */
public class RecoveryEvent {
    private String id;
    private String itemId;
    private String claimId;
    private String eventType;
    private LocalDateTime timestamp;
    private String actorUserId;
    private String description;

    public RecoveryEvent(String id, String itemId, String claimId, String eventType,
                         LocalDateTime timestamp, String actorUserId, String description) {
        this.id = id;
        this.itemId = itemId;
        this.claimId = claimId != null ? claimId : "";
        this.eventType = eventType;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.actorUserId = actorUserId != null ? actorUserId : "SYSTEM";
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(String actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s (by %s): %s",
                id, timestamp.toString(), eventType, actorUserId, description);
    }
}
