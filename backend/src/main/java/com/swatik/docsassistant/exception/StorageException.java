package com.swatik.docsassistant.exception;

// Throws when reading/writing a file on the local filesystem fails
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
