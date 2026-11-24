package com.intellirecover.exception;

/**
 * Thrown when a user attempts to submit a claim for an item that already has
 * an active pending claim or has already been approved/claimed.
 */
public class ClaimAlreadyExistsException extends IntelliRecoverException {
    public ClaimAlreadyExistsException(String message) {
        super(message);
    }
}
