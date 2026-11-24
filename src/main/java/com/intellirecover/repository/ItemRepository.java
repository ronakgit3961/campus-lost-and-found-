package com.intellirecover.repository;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.*;
import com.intellirecover.storage.FileStorageManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for Lost and Found items backed by CSV persistence.
 * Uses HashMap for high performance lookups and Java Streams for multi-criteria filtering.
 */
public class ItemRepository {
    private final Map<String, LostItem> lostItems = new HashMap<>();
    private final Map<String, FoundItem> foundItems = new HashMap<>();
    private final FileStorageManager storageManager;

    public ItemRepository(FileStorageManager storageManager) {
        this.storageManager = storageManager;
        load();
    }

    public synchronized void load() {
        try {
            lostItems.clear();
            foundItems.clear();

            for (LostItem item : storageManager.loadLostItems()) {
                lostItems.put(item.getId(), item);
            }
            for (FoundItem item : storageManager.loadFoundItems()) {
                foundItems.put(item.getId(), item);
            }
        } catch (StorageException e) {
            System.err.println("Error loading items: " + e.getMessage());
        }
    }

    public synchronized void saveLostItem(LostItem item) throws StorageException {
        lostItems.put(item.getId(), item);
        storageManager.saveLostItems(lostItems.values());
    }

    public synchronized void saveFoundItem(FoundItem item) throws StorageException {
        foundItems.put(item.getId(), item);
        storageManager.saveFoundItems(foundItems.values());
    }

    public synchronized Optional<LostItem> findLostItemById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(lostItems.get(id));
    }

    public synchronized Optional<FoundItem> findFoundItemById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(foundItems.get(id));
    }

    public synchronized Optional<Item> findItemById(String id) {
        if (id == null) return Optional.empty();
        if (lostItems.containsKey(id)) {
            return Optional.of(lostItems.get(id));
        }
        if (foundItems.containsKey(id)) {
            return Optional.of(foundItems.get(id));
        }
        return Optional.empty();
    }

    public synchronized List<LostItem> getAllLostItems() {
        return new ArrayList<>(lostItems.values());
    }

    public synchronized List<FoundItem> getAllFoundItems() {
        return new ArrayList<>(foundItems.values());
    }

    public synchronized List<LostItem> getActiveLostItems() {
        return lostItems.values().stream()
                .filter(i -> i.getStatus() == ItemStatus.REPORTED || i.getStatus() == ItemStatus.MATCH_PENDING)
                .collect(Collectors.toList());
    }

    public synchronized List<FoundItem> getActiveFoundItems() {
        return foundItems.values().stream()
                .filter(i -> i.getStatus() == ItemStatus.REPORTED || i.getStatus() == ItemStatus.MATCH_PENDING)
                .collect(Collectors.toList());
    }

    public synchronized List<LostItem> findLostItemsByReporter(String reporterUserId) {
        if (reporterUserId == null) return Collections.emptyList();
        return lostItems.values().stream()
                .filter(i -> reporterUserId.equals(i.getReporterUserId()))
                .collect(Collectors.toList());
    }

    public synchronized List<FoundItem> findFoundItemsByReporter(String reporterUserId) {
        if (reporterUserId == null) return Collections.emptyList();
        return foundItems.values().stream()
                .filter(i -> reporterUserId.equals(i.getReporterUserId()))
                .collect(Collectors.toList());
    }
}
