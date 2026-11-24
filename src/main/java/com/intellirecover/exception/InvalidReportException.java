package com.intellirecover.exception;

/**
 * Thrown when an item report contains invalid, incomplete, or malformed data.
 */
public class InvalidReportException extends IntelliRecoverException {
    public InvalidReportException(String message) {
        super(message);
    }
}
