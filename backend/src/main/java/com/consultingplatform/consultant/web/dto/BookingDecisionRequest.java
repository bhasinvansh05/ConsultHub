package com.consultingplatform.consultant.web.dto;

import jakarta.validation.constraints.Size;

public class BookingDecisionRequest {

    @Size(max = 500, message = "reason must be at most 500 characters")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
