package com.company.xmlgen.exception;

/**
 * Thrown when an underlying storage operation fails.
 */
public class StorageException extends ApplicationException {

    public StorageException(ErrorCode errorCode, String message) {
        super(errorCode, null, message);
    }

    public StorageException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, null, message, cause);
    }
}
