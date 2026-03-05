package com.consultingplatform.admin.service;

import com.consultingplatform.admin.web.dto.PolicyUpsertRequestDto;

public interface SystemPolicyService {
    PolicyUpsertResult upsertPolicy(String policyKey, PolicyUpsertRequestDto request);
}
