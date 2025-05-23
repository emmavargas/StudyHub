package com.example.studyapp.exceptions;

public class ResetPasswordException extends RuntimeException{
    private final String errorCode;

    public ResetPasswordException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
