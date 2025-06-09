package com.auth_service.exceptions;

public class MailServiceException extends RuntimeException {
    public MailServiceException(String message) {
        super(message);
    }
}
