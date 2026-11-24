package com.intellirecover.repository;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.Notification;
import com.intellirecover.storage.FileStorageManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for managing user notifications backed by CSV storage.
 */
public class NotificationRepository {
    private final List<Notification> notifications = new ArrayList<>();
    private final FileStorageManager storageManager;

    public NotificationRepository(FileStorageManager storageManager) {
        this.storageManager = storageManager;
        load();
    }

    public synchronized void load() {
        try {
            notifications.clear();
            notifications.addAll(storageManager.loadNotifications());
        } catch (StorageException e) {
            System.err.println("Error loading notifications: " + e.getMessage());
        }
    }

    public synchronized void save(Notification notif) throws StorageException {
        notifications.add(notif);
        storageManager.saveNotifications(notifications);
    }

    public synchronized List<Notification> findByRecipient(String userId) {
        if (userId == null) return Collections.emptyList();
        return notifications.stream()
                .filter(n -> userId.equalsIgnoreCase(n.getRecipientUserId()))
                .sorted(Comparator.comparing(Notification::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public synchronized void markAllAsRead(String userId) throws StorageException {
        if (userId == null) return;
        boolean changed = false;
        for (Notification n : notifications) {
            if (userId.equalsIgnoreCase(n.getRecipientUserId()) && !n.isRead()) {
                n.setRead(true);
                changed = true;
            }
        }
        if (changed) {
            storageManager.saveNotifications(notifications);
        }
    }

    public synchronized long getUnreadCount(String userId) {
        if (userId == null) return 0;
        return notifications.stream()
                .filter(n -> userId.equalsIgnoreCase(n.getRecipientUserId()) && !n.isRead())
                .count();
    }
}
