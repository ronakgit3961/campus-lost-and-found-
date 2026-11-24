package com.intellirecover.exception;

/**
 * Thrown when a requested resource (e.g., item, user, claim) cannot be found by ID.
 */
public class ResourceNotFoundException extends IntelliRecoverException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
