package com.consultingplatform.admin.web.dto;

import java.time.Instant;

public class PolicyResponseDto {
    private String policyKey;
    private String policyValue;
    private String updatedByAdminId;
    private Instant updatedAt;

    public PolicyResponseDto(String policyKey, String policyValue, String updatedByAdminId, Instant updatedAt) {
        this.policyKey = policyKey;
        this.policyValue = policyValue;
        this.updatedByAdminId = updatedByAdminId;
        this.updatedAt = updatedAt;
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public String getPolicyValue() {
        return policyValue;
    }

    public String getUpdatedByAdminId() {
        return updatedByAdminId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
