package com.intellirecover.service;

import com.intellirecover.exception.AuthenticationException;
import com.intellirecover.exception.StorageException;
import com.intellirecover.model.Administrator;
import com.intellirecover.model.Student;
import com.intellirecover.model.User;
import com.intellirecover.model.UserRole;
import com.intellirecover.repository.UserRepository;
import com.intellirecover.util.IDGenerator;
import com.intellirecover.util.InputValidator;
import com.intellirecover.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service managing user authentication, registration, password hashing, and active session.
 */
public class AuthenticationService {
    private final UserRepository userRepository;
    private User currentUser;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized User registerStudent(String username, String password, String fullName,
                                             String email, String studentId, String department,
                                             String phone)
            throws AuthenticationException, StorageException {

        validateCommon(username, password, fullName, email);

        if (!InputValidator.isNonEmpty(studentId)) {
            throw new AuthenticationException("Student ID cannot be empty.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        String id = IDGenerator.nextUserId();

        Student student = new Student(id, username.trim(), hash, salt, fullName.trim(),
                email.trim(), LocalDateTime.now(), studentId.trim(),
                department != null ? department.trim() : "",
                phone != null ? phone.trim() : "");

        userRepository.save(student);
        this.currentUser = student;
        return student;
    }

    public synchronized User registerAdmin(String username, String password, String fullName,
                                           String email, String staffId, String department)
            throws AuthenticationException, StorageException {

        validateCommon(username, password, fullName, email);

        if (!InputValidator.isNonEmpty(staffId)) {
            throw new AuthenticationException("Staff ID cannot be empty.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        String id = IDGenerator.nextUserId();

        Administrator admin = new Administrator(id, username.trim(), hash, salt, fullName.trim(),
                email.trim(), LocalDateTime.now(), staffId.trim(),
                department != null ? department.trim() : "");

        userRepository.save(admin);
        this.currentUser = admin;
        return admin;
    }

    public synchronized User login(String username, String password) throws AuthenticationException {
        if (!InputValidator.isNonEmpty(username) || !InputValidator.isNonEmpty(password)) {
            throw new AuthenticationException("Username and password must not be empty.");
        }

        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) {
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = optUser.get();
        boolean valid = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!valid) {
            throw new AuthenticationException("Invalid username or password.");
        }

        this.currentUser = user;
        return user;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean isStudent() {
        return currentUser != null && currentUser.getRole() == UserRole.STUDENT;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMINISTRATOR;
    }

    private void validateCommon(String username, String password, String fullName, String email)
            throws AuthenticationException {
        if (!InputValidator.isValidUsername(username)) {
            throw new AuthenticationException("Username must be alphanumeric (3-24 characters).");
        }
        if (password == null || password.length() < 4) {
            throw new AuthenticationException("Password must be at least 4 characters long.");
        }
        if (!InputValidator.isNonEmpty(fullName)) {
            throw new AuthenticationException("Full name cannot be empty.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new AuthenticationException("Invalid email format.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new AuthenticationException("Username '" + username + "' is already registered.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("Email '" + email + "' is already associated with an account.");
        }
    }
}
