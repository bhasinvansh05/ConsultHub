package com.consultingplatform.admin.service;

import com.consultingplatform.admin.web.dto.PolicyUpsertRequestDto;
import com.consultingplatform.admin.web.dto.PolicyResponseDto;
import java.util.Optional;

public interface SystemPolicyService {
    PolicyUpsertResult upsertPolicy(String policyKey, PolicyUpsertRequestDto request);

    <T> Optional<T> getPolicyConfig(String policyKey, Class<T> configClass);
    
    Optional<PolicyResponseDto> getPolicy(String policyKey);
}
