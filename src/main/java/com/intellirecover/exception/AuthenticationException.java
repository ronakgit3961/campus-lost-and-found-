package com.intellirecover.exception;

/**
 * Thrown when user authentication fails due to invalid credentials,
 * unregistered user, or insufficient authorization.
 */
public class AuthenticationException extends IntelliRecoverException {
    public AuthenticationException(String message) {
        super(message);
    }
}
