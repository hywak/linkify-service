package com.linkify.service.domain.exception;

public class FailedToPersistException extends RuntimeException {
    public FailedToPersistException(String message, Throwable cause) {
        super(message, cause);
    }
}
