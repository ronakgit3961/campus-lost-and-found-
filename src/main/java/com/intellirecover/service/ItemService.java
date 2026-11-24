package com.intellirecover.service;

import com.intellirecover.exception.InvalidReportException;
import com.intellirecover.exception.StorageException;
import com.intellirecover.model.*;
import com.intellirecover.repository.ItemRepository;
import com.intellirecover.util.FingerprintUtil;
import com.intellirecover.util.IDGenerator;
import com.intellirecover.util.InputValidator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service orchestrating Lost and Found item registrations, search, filtering,
 * and automated match triggers.
 */
public class ItemService {
    private final ItemRepository itemRepository;
    private final MatchingService matchingService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final RecoveryService recoveryService;
    private final NotificationService notificationService;

    public ItemService(ItemRepository itemRepository,
                       MatchingService matchingService,
                       DuplicateDetectionService duplicateDetectionService,
                       RecoveryService recoveryService,
                       NotificationService notificationService) {
        this.itemRepository = itemRepository;
        this.matchingService = matchingService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.recoveryService = recoveryService;
        this.notificationService = notificationService;
    }

    /**
     * Registers a new Lost Item report.
     */
    public synchronized LostItem reportLostItem(String name, ItemCategory category, String description,
                                                String location, LocalDate dateLost, String reporterUserId,
                                                String brand, String distinctiveAttributes, String rewardNotes)
            throws InvalidReportException, StorageException {

        validateItemData(name, category, description, location, dateLost);

        String id = IDGenerator.nextLostItemId();
        Set<String> keywords = matchingService.extractKeywords(name + " " + description);
        String fingerprint = FingerprintUtil.generateFingerprint(
                category.name(), name, brand, keywords, distinctiveAttributes);

        LostItem lostItem = new LostItem(id, name.trim(), category, description.trim(),
                location.trim(), LocalDate.now(), ItemStatus.REPORTED, reporterUserId,
                fingerprint, brand != null ? brand.trim() : "",
                distinctiveAttributes != null ? distinctiveAttributes.trim() : "",
                dateLost, rewardNotes != null ? rewardNotes.trim() : "");

        itemRepository.saveLostItem(lostItem);

        recoveryService.logEvent(id, null, "REPORTED", reporterUserId,
                String.format("Lost item reported: '%s' in category %s.", name, category.getDisplayName()));

        // Trigger automatic match check against active found items
        checkAndNotifyMatches(lostItem);

        return lostItem;
    }

    /**
     * Registers a new Found Item report with optional private verification questions.
     */
    public synchronized FoundItem reportFoundItem(String name, ItemCategory category, String description,
                                                  String location, LocalDate dateFound, String reporterUserId,
                                                  String brand, String distinctiveAttributes,
                                                  String storageLocation, List<VerificationQuestion> secretQuestions)
            throws InvalidReportException, StorageException {

        validateItemData(name, category, description, location, dateFound);

        String id = IDGenerator.nextFoundItemId();
        Set<String> keywords = matchingService.extractKeywords(name + " " + description);
        String fingerprint = FingerprintUtil.generateFingerprint(
                category.name(), name, brand, keywords, distinctiveAttributes);

        FoundItem foundItem = new FoundItem(id, name.trim(), category, description.trim(),
                location.trim(), LocalDate.now(), ItemStatus.REPORTED, reporterUserId,
                fingerprint, brand != null ? brand.trim() : "",
                distinctiveAttributes != null ? distinctiveAttributes.trim() : "",
                dateFound, storageLocation != null ? storageLocation.trim() : "Campus Security Desk",
                secretQuestions != null ? secretQuestions : new ArrayList<>(), null);

        itemRepository.saveFoundItem(foundItem);

        recoveryService.logEvent(id, null, "REPORTED", reporterUserId,
                String.format("Found item registered: '%s'. Stored at: %s.", name, foundItem.getStorageLocation()));

        // Trigger automatic match check against active lost items
        checkAndNotifyLostMatches(foundItem);

        return foundItem;
    }

    private void checkAndNotifyMatches(LostItem lostItem) {
        List<FoundItem> activeFound = itemRepository.getActiveFoundItems();
        List<MatchResult> matches = matchingService.findMatchesForLostItem(lostItem, activeFound);
        if (!matches.isEmpty()) {
            MatchResult top = matches.get(0);
            if (top.getTotalScore() >= 60.0) {
                recoveryService.logEvent(lostItem.getId(), null, "MATCH_DETECTED", "SYSTEM",
                        String.format("Potential match found with Found Item %s (Score: %.0f/100).",
                                top.getFoundItem().getId(), top.getTotalScore()));

                notificationService.sendNotification(lostItem.getReporterUserId(), "Potential Item Match!",
                        String.format("IntelliRecover detected a %s for your lost '%s' (Found ID: %s, Score: %.0f/100).",
                                top.getTier().getLabel(), lostItem.getName(), top.getFoundItem().getId(), top.getTotalScore()));
            }
        }
    }

    private void checkAndNotifyLostMatches(FoundItem foundItem) {
        List<LostItem> activeLost = itemRepository.getActiveLostItems();
        for (LostItem lost : activeLost) {
            MatchResult res = matchingService.calculateMatch(lost, foundItem);
            if (res.getTotalScore() >= 60.0) {
                recoveryService.logEvent(foundItem.getId(), null, "MATCH_DETECTED", "SYSTEM",
                        String.format("Potential match identified for Lost Item %s (Score: %.0f/100).",
                                lost.getId(), res.getTotalScore()));

                notificationService.sendNotification(lost.getReporterUserId(), "New Found Item Matches Your Report!",
                        String.format("A found item '%s' (ID: %s) was reported matching your lost item '%s' (Score: %.0f/100).",
                                foundItem.getName(), foundItem.getId(), lost.getName(), res.getTotalScore()));
            }
        }
    }

    /**
     * Checks for duplicates among existing items of the same type before saving.
     */
    public Optional<DuplicateDetectionService.DuplicateWarning> checkLostDuplicate(LostItem candidate) {
        return duplicateDetectionService.checkDuplicate(candidate, itemRepository.getAllLostItems());
    }

    public Optional<DuplicateDetectionService.DuplicateWarning> checkFoundDuplicate(FoundItem candidate) {
        return duplicateDetectionService.checkDuplicate(candidate, itemRepository.getAllFoundItems());
    }

    /**
     * Multi-criteria search across Lost Items.
     */
    public List<LostItem> searchLostItems(String query, ItemCategory category, String location,
                                          LocalDate fromDate, LocalDate toDate) {
        return itemRepository.getAllLostItems().stream()
                .filter(i -> filterItem(i, query, category, location, fromDate, toDate))
                .collect(Collectors.toList());
    }

    /**
     * Multi-criteria search across Found Items.
     */
    public List<FoundItem> searchFoundItems(String query, ItemCategory category, String location,
                                            LocalDate fromDate, LocalDate toDate) {
        return itemRepository.getAllFoundItems().stream()
                .filter(i -> filterItem(i, query, category, location, fromDate, toDate))
                .collect(Collectors.toList());
    }

    /**
     * Public search for guests and general catalog browsing.
     * Excludes private questions and returned/archived items by default.
     */
    public List<FoundItem> searchPublicFoundItems(String query, ItemCategory category, String location) {
        return itemRepository.getAllFoundItems().stream()
                .filter(i -> i.getStatus() != ItemStatus.RETURNED && i.getStatus() != ItemStatus.ARCHIVED)
                .filter(i -> filterItem(i, query, category, location, null, null))
                .collect(Collectors.toList());
    }

    private boolean filterItem(Item item, String query, ItemCategory category, String location,
                               LocalDate fromDate, LocalDate toDate) {
        if (category != null && item.getCategory() != category) {
            return false;
        }

        if (location != null && !location.trim().isEmpty()) {
            if (!item.getLocation().toLowerCase().contains(location.trim().toLowerCase())) {
                return false;
            }
        }

        if (fromDate != null && item.getIncidentDate().isBefore(fromDate)) {
            return false;
        }

        if (toDate != null && item.getIncidentDate().isAfter(toDate)) {
            return false;
        }

        if (query != null && !query.trim().isEmpty()) {
            String q = query.toLowerCase();
            boolean inName = item.getName().toLowerCase().contains(q);
            boolean inDesc = item.getDescription().toLowerCase().contains(q);
            boolean inBrand = item.getBrand() != null && item.getBrand().toLowerCase().contains(q);
            boolean inId = item.getId().toLowerCase().contains(q);
            return inName || inDesc || inBrand || inId;
        }

        return true;
    }

    private void validateItemData(String name, ItemCategory category, String description,
                                  String location, LocalDate incidentDate)
            throws InvalidReportException {

        if (!InputValidator.isNonEmpty(name)) {
            throw new InvalidReportException("Item name cannot be empty.");
        }
        if (category == null) {
            throw new InvalidReportException("Item category must be specified.");
        }
        if (!InputValidator.isNonEmpty(description) || description.trim().length() < 5) {
            throw new InvalidReportException("Description must contain at least 5 characters.");
        }
        if (!InputValidator.isNonEmpty(location)) {
            throw new InvalidReportException("Location cannot be empty.");
        }
        if (!InputValidator.isValidPastOrPresentDate(incidentDate)) {
            throw new InvalidReportException("Incident date cannot be in the future.");
        }
    }

    public Optional<LostItem> getLostItemById(String id) {
        return itemRepository.findLostItemById(id);
    }

    public Optional<FoundItem> getFoundItemById(String id) {
        return itemRepository.findFoundItemById(id);
    }

    public Optional<Item> getItemById(String id) {
        return itemRepository.findItemById(id);
    }

    public List<LostItem> getLostItemsByUser(String userId) {
        return itemRepository.findLostItemsByReporter(userId);
    }

    public List<FoundItem> getFoundItemsByUser(String userId) {
        return itemRepository.findFoundItemsByReporter(userId);
    }

    public List<LostItem> getAllLostItems() {
        return itemRepository.getAllLostItems();
    }

    public List<FoundItem> getAllFoundItems() {
        return itemRepository.getAllFoundItems();
    }

    public void updateLostItemStatus(LostItem item, ItemStatus status) throws StorageException {
        item.setStatus(status);
        itemRepository.saveLostItem(item);
    }

    public void updateFoundItemStatus(FoundItem item, ItemStatus status) throws StorageException {
        item.setStatus(status);
        itemRepository.saveFoundItem(item);
    }
}
