package com.intellirecover.service;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.Notification;
import com.intellirecover.repository.NotificationRepository;
import com.intellirecover.util.IDGenerator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for delivering system updates and alerts to students and staff.
 */
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public synchronized void sendNotification(String recipientUserId, String title, String message) {
        try {
            String id = IDGenerator.nextNotificationId();
            Notification notif = new Notification(id, recipientUserId, title, message, LocalDateTime.now(), false);
            notificationRepository.save(notif);
        } catch (StorageException e) {
            System.err.println("Failed to save notification: " + e.getMessage());
        }
    }

    public synchronized List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByRecipient(userId);
    }

    public synchronized void markAllAsRead(String userId) {
        try {
            notificationRepository.markAllAsRead(userId);
        } catch (StorageException e) {
            System.err.println("Failed to mark notifications as read: " + e.getMessage());
        }
    }

    public synchronized long getUnreadCount(String userId) {
        return notificationRepository.getUnreadCount(userId);
    }
}
