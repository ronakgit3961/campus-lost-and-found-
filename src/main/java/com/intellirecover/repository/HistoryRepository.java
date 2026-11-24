package com.intellirecover.repository;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.RecoveryEvent;
import com.intellirecover.storage.FileStorageManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for persisting and retrieving audit history events.
 * Backed by CSV file storage and ordered chronologically.
 */
public class HistoryRepository {
    private final List<RecoveryEvent> history = new ArrayList<>();
    private final FileStorageManager storageManager;

    public HistoryRepository(FileStorageManager storageManager) {
        this.storageManager = storageManager;
        load();
    }

    public synchronized void load() {
        try {
            history.clear();
            history.addAll(storageManager.loadHistory());
        } catch (StorageException e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }

    public synchronized void save(RecoveryEvent event) throws StorageException {
        history.add(event);
        storageManager.saveHistory(history);
    }

    public synchronized List<RecoveryEvent> findByItemId(String itemId) {
        if (itemId == null) return Collections.emptyList();
        return history.stream()
                .filter(e -> itemId.equalsIgnoreCase(e.getItemId()))
                .sorted(Comparator.comparing(RecoveryEvent::getTimestamp))
                .collect(Collectors.toList());
    }

    public synchronized List<RecoveryEvent> findAll() {
        return history.stream()
                .sorted(Comparator.comparing(RecoveryEvent::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
}
