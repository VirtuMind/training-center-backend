package com.marketplace.trainingcenter.exception;

import com.marketplace.trainingcenter.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(ApiException ex) {
        log.error("API Exception: {}", ex.getMessage());
        return new ResponseEntity<>(
                ApiResponse.error(ex.getCode(), ex.getMessage()),
                ex.getStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation Exception: {}", ex.getMessage());
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + ": " + errorMessage);
        });
        return new ResponseEntity<>(
                ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint Violation: {}", ex.getMessage());
        List<String> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> 
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage())
        );
        return new ResponseEntity<>(
                ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad Credentials: {}", ex.getMessage());
        return new ResponseEntity<>(
                ApiResponse.error("INVALID_CREDENTIALS", "Invalid username or password"),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access Denied: {}", ex.getMessage());
        return new ResponseEntity<>(
                ApiResponse.error("ACCESS_DENIED", "You don't have permission to access this resource"),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type Mismatch: {}", ex.getMessage());
        String message = String.format("Parameter '%s' should be of type '%s'", 
                ex.getName(), ex.getRequiredType().getSimpleName());
        return new ResponseEntity<>(
                ApiResponse.error("TYPE_MISMATCH", message),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<?>> handleIOException(IOException ex) {
        log.error("IO Exception: {}", ex.getMessage());
        return new ResponseEntity<>(
                ApiResponse.error("IO_ERROR", "Error processing file: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unhandled Exception: ", ex);
        return new ResponseEntity<>(
                ApiResponse.error("SERVER_ERROR", "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
