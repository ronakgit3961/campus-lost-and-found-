package com.intellirecover.model;

import java.time.LocalDateTime;

/**
 * Model representing an alert or status update for a student or administrator.
 */
public class Notification {
    private String id;
    private String recipientUserId;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;

    public Notification(String id, String recipientUserId, String title,
                        String message, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (%s)",
                read ? "READ" : "UNREAD", title, message, timestamp.toString());
    }
}
