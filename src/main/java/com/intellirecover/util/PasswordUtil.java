package com.intellirecover.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for salted password hashing using standard Java MessageDigest (SHA-256).
 * Demonstrates cryptographic hashing without external dependencies.
 */
public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Generates a cryptographically secure random 16-byte salt, Base64 encoded.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Computes the SHA-256 hash of a plaintext password combined with a salt.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in environment", e);
        }
    }

    /**
     * Verifies whether a given plaintext password matches the stored salt and hash.
     */
    public static boolean verifyPassword(String candidatePassword, String salt, String storedHash) {
        if (candidatePassword == null || salt == null || storedHash == null) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword, salt);
        return candidateHash.equals(storedHash);
    }
}
