package com.intellirecover.repository;

import com.intellirecover.exception.StorageException;
import com.intellirecover.model.User;
import com.intellirecover.storage.FileStorageManager;

import java.util.*;

/**
 * In-memory repository for Users backed by CSV file persistence.
 * Uses HashMap for O(1) lookup by ID, username, and email.
 */
public class UserRepository {
    private final Map<String, User> usersById = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final FileStorageManager storageManager;

    public UserRepository(FileStorageManager storageManager) {
        this.storageManager = storageManager;
        load();
    }

    public synchronized void load() {
        try {
            usersById.clear();
            usersByUsername.clear();
            usersByEmail.clear();

            List<User> list = storageManager.loadUsers();
            for (User u : list) {
                indexUser(u);
            }
        } catch (StorageException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void indexUser(User u) {
        usersById.put(u.getId(), u);
        usersByUsername.put(u.getUsername().toLowerCase(), u);
        usersByEmail.put(u.getEmail().toLowerCase(), u);
    }

    public synchronized void save(User user) throws StorageException {
        indexUser(user);
        storageManager.saveUsers(usersById.values());
    }

    public synchronized Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(usersById.get(id));
    }

    public synchronized Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(usersByUsername.get(username.toLowerCase().trim()));
    }

    public synchronized Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return Optional.ofNullable(usersByEmail.get(email.toLowerCase().trim()));
    }

    public synchronized List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    public synchronized boolean existsByUsername(String username) {
        return username != null && usersByUsername.containsKey(username.toLowerCase().trim());
    }

    public synchronized boolean existsByEmail(String email) {
        return email != null && usersByEmail.containsKey(email.toLowerCase().trim());
    }
}
