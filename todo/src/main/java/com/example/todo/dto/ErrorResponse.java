package com.example.todo.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
