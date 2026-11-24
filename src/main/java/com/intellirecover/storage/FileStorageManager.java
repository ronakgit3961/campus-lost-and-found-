package com.intellirecover.storage;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.*;
import com.intellirecover.util.DateUtil;
import com.intellirecover.util.IDGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages physical CSV file persistence in the data directory.
 * Provides fault-tolerant loading, safe saving, and automatic ID counter synchronization.
 */
public class FileStorageManager {
    private final Path dataDir;
    private final Path usersFile;
    private final Path lostItemsFile;
    private final Path foundItemsFile;
    private final Path claimsFile;
    private final Path historyFile;
    private final Path notificationsFile;

    public FileStorageManager(String dataDirPath) {
        this.dataDir = Paths.get(dataDirPath);
        this.usersFile = dataDir.resolve("users.csv");
        this.lostItemsFile = dataDir.resolve("lost_items.csv");
        this.foundItemsFile = dataDir.resolve("found_items.csv");
        this.claimsFile = dataDir.resolve("claims.csv");
        this.historyFile = dataDir.resolve("recovery_history.csv");
        this.notificationsFile = dataDir.resolve("notifications.csv");
        initDataDirectory();
    }

    public FileStorageManager() {
        this("data");
    }

    private void initDataDirectory() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            createIfMissing(usersFile, "id,username,passwordHash,salt,fullName,email,role,createdAt,studentOrStaffId,department,phone");
            createIfMissing(lostItemsFile, "id,name,category,description,location,reportedDate,status,reporterUserId,fingerprint,brand,distinctiveAttributes,dateLost,rewardNotes");
            createIfMissing(foundItemsFile, "id,name,category,description,location,reportedDate,status,reporterUserId,fingerprint,brand,distinctiveAttributes,dateFound,storageLocation,secretQuestions,currentClaimId");
            createIfMissing(claimsFile, "id,foundItemId,claimantUserId,submissionDate,submittedAnswers,verificationScore,status,reviewNotes,reviewedByAdminId,reviewedAt");
            createIfMissing(historyFile, "id,itemId,claimId,eventType,timestamp,actorUserId,description");
            createIfMissing(notificationsFile, "id,recipientUserId,title,message,timestamp,read");
        } catch (IOException e) {
            System.err.println("Warning: Could not initialize data directory: " + e.getMessage());
        }
    }

    private void createIfMissing(Path path, String header) throws IOException {
        if (!Files.exists(path)) {
            Files.writeString(path, header + System.lineSeparator(), StandardCharsets.UTF_8);
        }
    }

    // ==========================================
    // USERS PERSISTENCE
    // ==========================================
    public List<User> loadUsers() throws StorageException {
        List<User> list = new ArrayList<>();
        if (!Files.exists(usersFile)) return list;

        try (BufferedReader reader = Files.newBufferedReader(usersFile, StandardCharsets.UTF_8)) {
            String header = reader.readLine(); // skip header
            List<String> record;
            while ((record = CSVUtil.readRecord(reader)) != null) {
                if (record.size() < 8) continue;
                try {
                    String id = record.get(0).trim();
                    String username = record.get(1).trim();
                    String pwdHash = record.get(2).trim();
                    String salt = record.get(3).trim();
                    String fullName = record.get(4).trim();
                    String email = record.get(5).trim();
                    UserRole role = UserRole.valueOf(record.get(6).trim());
                    LocalDateTime createdAt = DateUtil.parseDateTime(record.get(7).trim());

                    IDGenerator.synchronizeSequence(id);

                    if (role == UserRole.STUDENT) {
                        String studentId = record.size() > 8 ? record.get(8).trim() : "";
                        String dept = record.size() > 9 ? record.get(9).trim() : "";
                        String phone = record.size() > 10 ? record.get(10).trim() : "";
                        list.add(new Student(id, username, pwdHash, salt, fullName, email, createdAt, studentId, dept, phone));
                    } else if (role == UserRole.ADMINISTRATOR) {
                        String staffId = record.size() > 8 ? record.get(8).trim() : "";
                        String dept = record.size() > 9 ? record.get(9).trim() : "";
                        list.add(new Administrator(id, username, pwdHash, salt, fullName, email, createdAt, staffId, dept));
                    }
                } catch (Exception e) {
                    System.err.println("Skipping malformed user record: " + record);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read users from file: " + usersFile, e);
        }
        return list;
    }

    public synchronized void saveUsers(Collection<User> users) throws StorageException {
        List<String> lines = new ArrayList<>();
        lines.add("id,username,passwordHash,salt,fullName,email,role,createdAt,studentOrStaffId,department,phone");

        for (User u : users) {
            List<String> fields = new ArrayList<>();
            fields.add(u.getId());
            fields.add(u.getUsername());
            fields.add(u.getPasswordHash());
            fields.add(u.getSalt());
            fields.add(u.getFullName());
            fields.add(u.getEmail());
            fields.add(u.getRole().name());
            fields.add(DateUtil.formatDateTime(u.getCreatedAt()));

            if (u instanceof Student s) {
                fields.add(s.getStudentId());
                fields.add(s.getDepartment());
                fields.add(s.getPhone());
            } else if (u instanceof Administrator a) {
                fields.add(a.getStaffId());
                fields.add(a.getDepartment());
                fields.add("");
            } else {
                fields.add("");
                fields.add("");
                fields.add("");
            }
            lines.add(CSVUtil.formatLine(fields));
        }

        atomicWrite(usersFile, lines);
    }

    // ==========================================
    // LOST ITEMS PERSISTENCE
    // ==========================================
    public List<LostItem> loadLostItems() throws StorageException {
        List<LostItem> list = new ArrayList<>();
        if (!Files.exists(lostItemsFile)) return list;

        try (BufferedReader reader = Files.newBufferedReader(lostItemsFile, StandardCharsets.UTF_8)) {
            reader.readLine(); // skip header
            List<String> record;
            while ((record = CSVUtil.readRecord(reader)) != null) {
                if (record.size() < 13) continue;
                try {
                    String id = record.get(0).trim();
                    String name = record.get(1).trim();
                    ItemCategory cat = ItemCategory.fromString(record.get(2).trim());
                    String desc = record.get(3).trim();
                    String loc = record.get(4).trim();
                    LocalDate reportedDate = DateUtil.parseDate(record.get(5).trim());
                    ItemStatus status = ItemStatus.fromString(record.get(6).trim());
                    String reporter = record.get(7).trim();
                    String fp = record.get(8).trim();
                    String brand = record.get(9).trim();
                    String attrs = record.get(10).trim();
                    LocalDate dateLost = DateUtil.parseDate(record.get(11).trim());
                    String rewardNotes = record.get(12).trim();

                    IDGenerator.synchronizeSequence(id);

                    list.add(new LostItem(id, name, cat, desc, loc, reportedDate, status,
                            reporter, fp, brand, attrs, dateLost, rewardNotes));
                } catch (Exception e) {
                    System.err.println("Skipping malformed lost item record: " + record);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read lost items from file: " + lostItemsFile, e);
        }
        return list;
    }

    public synchronized void saveLostItems(Collection<LostItem> items) throws StorageException {
        List<String> lines = new ArrayList<>();
        lines.add("id,name,category,description,location,reportedDate,status,reporterUserId,fingerprint,brand,distinctiveAttributes,dateLost,rewardNotes");

        for (LostItem item : items) {
            List<String> fields = Arrays.asList(
                    item.getId(),
                    item.getName(),
                    item.getCategory().name(),
                    item.getDescription(),
                    item.getLocation(),
                    DateUtil.formatDate(item.getReportedDate()),
                    item.getStatus().name(),
                    item.getReporterUserId(),
                    item.getFingerprint(),
                    item.getBrand(),
                    item.getDistinctiveAttributes(),
                    DateUtil.formatDate(item.getDateLost()),
                    item.getRewardNotes()
            );
            lines.add(CSVUtil.formatLine(fields));
        }

        atomicWrite(lostItemsFile, lines);
    }

    // ==========================================
    // FOUND ITEMS PERSISTENCE
    // ==========================================
    public List<FoundItem> loadFoundItems() throws StorageException {
        List<FoundItem> list = new ArrayList<>();
        if (!Files.exists(foundItemsFile)) return list;

        try (BufferedReader reader = Files.newBufferedReader(foundItemsFile, StandardCharsets.UTF_8)) {
            reader.readLine(); // skip header
            List<String> record;
            while ((record = CSVUtil.readRecord(reader)) != null) {
                if (record.size() < 15) continue;
                try {
                    String id = record.get(0).trim();
                    String name = record.get(1).trim();
                    ItemCategory cat = ItemCategory.fromString(record.get(2).trim());
                    String desc = record.get(3).trim();
                    String loc = record.get(4).trim();
                    LocalDate reportedDate = DateUtil.parseDate(record.get(5).trim());
                    ItemStatus status = ItemStatus.fromString(record.get(6).trim());
                    String reporter = record.get(7).trim();
                    String fp = record.get(8).trim();
                    String brand = record.get(9).trim();
                    String attrs = record.get(10).trim();
                    LocalDate dateFound = DateUtil.parseDate(record.get(11).trim());
                    String storageLoc = record.get(12).trim();
                    String secretQuestionsRaw = record.get(13).trim();
                    String currentClaimId = record.get(14).trim();

                    IDGenerator.synchronizeSequence(id);

                    List<VerificationQuestion> questions = deserializeQuestions(secretQuestionsRaw);

                    list.add(new FoundItem(id, name, cat, desc, loc, reportedDate, status,
                            reporter, fp, brand, attrs, dateFound, storageLoc,
                            questions, currentClaimId.isEmpty() ? null : currentClaimId));
                } catch (Exception e) {
                    System.err.println("Skipping malformed found item record: " + record);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read found items from file: " + foundItemsFile, e);
        }
        return list;
    }

    public synchronized void saveFoundItems(Collection<FoundItem> items) throws StorageException {
        List<String> lines = new ArrayList<>();
        lines.add("id,name,category,description,location,reportedDate,status,reporterUserId,fingerprint,brand,distinctiveAttributes,dateFound,storageLocation,secretQuestions,currentClaimId");

        for (FoundItem item : items) {
            List<String> fields = Arrays.asList(
                    item.getId(),
                    item.getName(),
                    item.getCategory().name(),
                    item.getDescription(),
                    item.getLocation(),
                    DateUtil.formatDate(item.getReportedDate()),
                    item.getStatus().name(),
                    item.getReporterUserId(),
                    item.getFingerprint(),
                    item.getBrand(),
                    item.getDistinctiveAttributes(),
                    DateUtil.formatDate(item.getDateFound()),
                    item.getStorageLocation(),
                    serializeQuestions(item.getSecretQuestions()),
                    item.getCurrentClaimId() != null ? item.getCurrentClaimId() : ""
            );
            lines.add(CSVUtil.formatLine(fields));
        }

        atomicWrite(foundItemsFile, lines);
    }

    // ==========================================
    // CLAIMS PERSISTENCE
    // ==========================================
    public List<Claim> loadClaims() throws StorageException {
        List<Claim> list = new ArrayList<>();
        if (!Files.exists(claimsFile)) return list;

        try (BufferedReader reader = Files.newBufferedReader(claimsFile, StandardCharsets.UTF_8)) {
            reader.readLine(); // skip header
            List<String> record;
            while ((record = CSVUtil.readRecord(reader)) != null) {
                if (record.size() < 10) continue;
                try {
                    String id = record.get(0).trim();
                    String foundItemId = record.get(1).trim();
                    String claimantUserId = record.get(2).trim();
                    LocalDateTime subDate = DateUtil.parseDateTime(record.get(3).trim());
                    String answersRaw = record.get(4).trim();
                    double score = Double.parseDouble(record.get(5).trim());
                    ClaimStatus status = ClaimStatus.fromString(record.get(6).trim());
                    String reviewNotes = record.get(7).trim();
                    String adminId = record.get(8).trim();
                    LocalDateTime revDate = DateUtil.parseDateTime(record.get(9).trim());

                    IDGenerator.synchronizeSequence(id);

                    List<String> answers = deserializeList(answersRaw);

                    list.add(new Claim(id, foundItemId, claimantUserId, subDate, answers,
                            score, status, reviewNotes, adminId.isEmpty() ? null : adminId, revDate));
                } catch (Exception e) {
                    System.err.println("Skipping malformed claim record: " + record);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read claims from file: " + claimsFile, e);
        }
        return list;
    }

    public synchronized void saveClaims(Collection<Claim> claims) throws StorageException {
        List<String> lines = new ArrayList<>();
        lines.add("id,foundItemId,claimantUserId,submissionDate,submittedAnswers,verificationScore,status,reviewNotes,reviewedByAdminId,reviewedAt");

        for (Claim c : claims) {
            List<String> fields = Arrays.asList(
                    c.getId(),
                    c.getFoundItemId(),
                    c.getClaimantUserId(),
                    DateUtil.formatDateTime(c.getSubmissionDate()),
                    serializeList(c.getSubmittedAnswers()),
                    String.format(Locale.US, "%.1f", c.getVerificationScore()),
                    c.getStatus().name(),
                    c.getReviewNotes(),
                    c.getReviewedByAdminId() != null ? c.getReviewedByAdminId() : "",
                    DateUtil.formatDateTime(c.getReviewedAt())
            );
            lines.add(CSVUtil.formatLine(fields));
        }

        atomicWrite(claimsFile, lines);
    }

    // ==========================================
    // RECOVERY HISTORY PERSISTENCE
    // ==========================================
    public List<RecoveryEvent> loadHistory() throws StorageException {
        List<RecoveryEvent> list = new ArrayList<>();
        if (!Files.exists(historyFile)) return list;

        try (BufferedReader reader = Files.newBufferedReader(historyFile, StandardCharsets.UTF_8)) {
            reader.readLine(); // skip header
            List<String> record;
            while ((record = CSVUtil.readRecord(reader)) != null) {
                if (record.size() < 7) continue;
                try {
                    String id = record.get(0).trim();
                    String itemId = record.get(1).trim();
                    String claimId = record.get(2).trim();
                    String eventType = record.get(3).trim();
                    LocalDateTime time = DateUtil.parseDateTime(record.get(4).trim());
                    String actor = record.get(5).trim();
                    String desc = record.get(6).trim();

                    IDGenerator.synchronizeSequence(id);

                    list.add(new RecoveryEvent(id, itemId, claimId, eventType, time, actor, desc));
                } catch (Exception e) {
                    System.err.println("Skipping malformed history record: " + record);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read history from file: " + historyFile, e);
        }
        return list;
    }

    public synchronized void saveHistory(Collection<RecoveryEvent> events) throws StorageException {
        List<String> lines = new ArrayList<>();
        lines.add("id,itemId,claimId,eventType,timestamp,actorUserId,description");

        for (RecoveryEvent ev : events) {
            List<String> fields = Arrays.asList(
                    ev.getId(),
                    ev.getItemId(),
                    ev.getClaimId() != null ? ev.getClaimId() : "",
                    ev.getEventType(),
                    DateUtil.formatDateTime(ev.getTimestamp()),
                    ev.getActorUserId(),
                    ev.getDescription()
            );
            lines.add(CSVUtil.formatLine(fields));
        }

        atomicWrite(historyFile, lines);
    }

    // ==========================================
    // NOTIFICATIONS PERSISTENCE
    // ==========================================
    public List<Notification> loadNotifications() throws StorageException {
        List<Notification> list = new ArrayList<>();
        if (!Files.exists(notificationsFile)) return list;

        try (BufferedReader reader = Files.newBufferedReader(notificationsFile, StandardCharsets.UTF_8)) {
            reader.readLine(); // skip header
            List<String> record;
            while ((record = CSVUtil.readRecord(reader)) != null) {
                if (record.size() < 6) continue;
                try {
                    String id = record.get(0).trim();
                    String recipient = record.get(1).trim();
                    String title = record.get(2).trim();
                    String msg = record.get(3).trim();
                    LocalDateTime time = DateUtil.parseDateTime(record.get(4).trim());
                    boolean read = Boolean.parseBoolean(record.get(5).trim());

                    IDGenerator.synchronizeSequence(id);

                    list.add(new Notification(id, recipient, title, msg, time, read));
                } catch (Exception e) {
                    System.err.println("Skipping malformed notification record: " + record);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read notifications from file: " + notificationsFile, e);
        }
        return list;
    }

    public synchronized void saveNotifications(Collection<Notification> notifs) throws StorageException {
        List<String> lines = new ArrayList<>();
        lines.add("id,recipientUserId,title,message,timestamp,read");

        for (Notification n : notifs) {
            List<String> fields = Arrays.asList(
                    n.getId(),
                    n.getRecipientUserId(),
                    n.getTitle(),
                    n.getMessage(),
                    DateUtil.formatDateTime(n.getTimestamp()),
                    String.valueOf(n.isRead())
            );
            lines.add(CSVUtil.formatLine(fields));
        }

        atomicWrite(notificationsFile, lines);
    }

    // ==========================================
    // HELPER ENCODING & SAFE ATOMIC WRITES
    // ==========================================
    private void atomicWrite(Path target, List<String> lines) throws StorageException {
        Path tempFile = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.write(tempFile, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Fallback standard move if atomic not supported by filesystem
            try {
                if (Files.exists(tempFile)) {
                    Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                throw new StorageException("Failed to persist data file: " + target, ex);
            }
        }
    }

    private String serializeQuestions(List<VerificationQuestion> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append("|||");
            VerificationQuestion q = list.get(i);
            sb.append(q.getQuestion().replace(":::", " ")).append(":::")
              .append(q.getSecretAnswer().replace(":::", " "));
        }
        return sb.toString();
    }

    private List<VerificationQuestion> deserializeQuestions(String raw) {
        List<VerificationQuestion> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return list;
        String[] pairs = raw.split("\\|\\|\\|");
        for (String pair : pairs) {
            String[] parts = pair.split(":::", 2);
            if (parts.length == 2) {
                list.add(new VerificationQuestion(parts[0], parts[1]));
            }
        }
        return list;
    }

    private String serializeList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join("|||", list);
    }

    private List<String> deserializeList(String raw) {
        List<String> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return list;
        String[] parts = raw.split("\\|\\|\\|");
        list.addAll(Arrays.asList(parts));
        return list;
    }
}
