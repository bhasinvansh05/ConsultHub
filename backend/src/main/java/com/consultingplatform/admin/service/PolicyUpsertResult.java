package com.consultingplatform.admin.service;

import com.consultingplatform.admin.web.dto.PolicyResponseDto;

public class PolicyUpsertResult {
    private final boolean created;
    private final PolicyResponseDto response;

    public PolicyUpsertResult(boolean created, PolicyResponseDto response) {
        this.created = created;
        this.response = response;
    }

    public boolean isCreated() {
        return created;
    }

    public PolicyResponseDto getResponse() {
        return response;
    }
}
