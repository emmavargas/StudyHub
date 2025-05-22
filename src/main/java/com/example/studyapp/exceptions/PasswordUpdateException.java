package com.example.studyapp.exceptions;

public class PasswordUpdateException extends RuntimeException {
    private final String errorCode;

    public PasswordUpdateException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
