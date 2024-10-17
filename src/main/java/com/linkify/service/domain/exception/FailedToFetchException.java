package com.linkify.service.domain.exception;

public class FailedToFetchException extends RuntimeException {
    public FailedToFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
