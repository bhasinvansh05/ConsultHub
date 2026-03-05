package com.consultingplatform.admin.web.dto;

import jakarta.validation.constraints.NotBlank;

public class PolicyUpsertRequestDto {
    @NotBlank(message = "adminId is required")
    private String adminId;

    @NotBlank(message = "policyValue is required")
    private String policyValue;

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getPolicyValue() {
        return policyValue;
    }

    public void setPolicyValue(String policyValue) {
        this.policyValue = policyValue;
    }
}
