package com.marketplace.trainingcenter.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
