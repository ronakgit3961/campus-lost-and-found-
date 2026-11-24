package com.intellirecover.exception;

/**
 * Thrown when an authenticated user attempts to perform an operation
 * not permitted by their role (e.g. Student accessing Administrator functions).
 */
public class UnauthorizedException extends IntelliRecoverException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
