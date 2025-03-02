package com.byteforge.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{
    private final String details;
    private final HttpStatus status;

    public CustomException(String message, String details, HttpStatus status){
        super(message);
        this.details = details;
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
