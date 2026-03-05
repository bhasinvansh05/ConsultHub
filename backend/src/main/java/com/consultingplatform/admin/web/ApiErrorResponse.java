package com.consultingplatform.admin.web;

import java.time.Instant;
import java.util.Map;

public class ApiErrorResponse {
    private final String code;
    private final String message;
    private final Instant timestamp;
    private final Map<String, String> details;

    public ApiErrorResponse(String code, String message) {
        this(code, message, null);
    }

    public ApiErrorResponse(String code, String message, Map<String, String> details) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
