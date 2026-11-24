package com.intellirecover.exception;

/**
 * Thrown when an error occurs during file I/O, CSV persistence,
 * or record serialization/deserialization.
 */
public class StorageException extends IntelliRecoverException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
