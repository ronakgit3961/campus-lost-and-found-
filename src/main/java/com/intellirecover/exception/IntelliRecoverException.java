package com.intellirecover.exception;

/**
 * Base custom checked exception for IntelliRecover system.
 * Demonstrates Java exception hierarchy and inheritance.
 */
public class IntelliRecoverException extends Exception {
    public IntelliRecoverException(String message) {
        super(message);
    }

    public IntelliRecoverException(String message, Throwable cause) {
        super(message, cause);
    }
}
