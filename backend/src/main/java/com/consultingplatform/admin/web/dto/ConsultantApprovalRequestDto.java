package com.consultingplatform.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConsultantApprovalRequestDto {
    @NotBlank(message = "adminId is required")
    private String adminId;

    @NotNull(message = "decision is required")
    private ConsultantApprovalDecision decision;

    private String reason;

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public ConsultantApprovalDecision getDecision() {
        return decision;
    }

    public void setDecision(ConsultantApprovalDecision decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
