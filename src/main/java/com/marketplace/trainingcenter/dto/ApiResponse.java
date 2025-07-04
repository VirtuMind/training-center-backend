package com.marketplace.trainingcenter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;// Instead of LocalDateTime
    private String timestamp = LocalDateTime.now().toString();
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Operation successful", data);
    }

    public static ApiResponse<?> error(String errorCode, String errorMessage) {
        ErrorDetails error = new ErrorDetails(errorCode, errorMessage, null);
        return ApiResponse.builder()
                .success(false)
                .error(error)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static ApiResponse<?> error(String errorCode, String errorMessage, List<String> details) {
        ErrorDetails error = new ErrorDetails(errorCode, errorMessage, details);
        return ApiResponse.builder()
                .success(false)
                .error(error)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private List<String> details;
    }
}
