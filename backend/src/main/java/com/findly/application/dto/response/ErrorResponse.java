package com.findly.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;
    private final String path;
    private final Map<String, String> errors; // For validation errors

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String error, String code, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .code(code)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse withValidationErrors(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .code("VALIDATION_ERROR")
                .message(message)
                .path(path)
                .errors(validationErrors)
                .build();
    }
}
