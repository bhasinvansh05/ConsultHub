package com.consultingplatform.admin.service;

import com.consultingplatform.admin.domain.SystemPolicy;
import com.consultingplatform.admin.repository.SystemPolicyRepository;
import com.consultingplatform.admin.web.dto.PolicyResponseDto;
import com.consultingplatform.admin.web.dto.PolicyUpsertRequestDto;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class SystemPolicyServiceImpl implements SystemPolicyService {

    private static final Set<String> ALLOWED_KEYS = Set.of(
        "CANCELLATION_RULES",
        "PRICING_STRATEGY",
        "NOTIFICATION_SETTINGS",
        "REFUND_POLICY",
        "MODEL"
    );

    private final SystemPolicyRepository repository;

    public SystemPolicyServiceImpl(SystemPolicyRepository repository) {
        this.repository = repository;
    }

    @Override
    public PolicyUpsertResult upsertPolicy(String policyKey, PolicyUpsertRequestDto request) {
        if (isBlank(policyKey)) {
            throw new IllegalArgumentException("policyKey is required");
        }
        if (request == null || isBlank(request.getAdminId())) {
            throw new IllegalArgumentException("adminId is required");
        }
        if (isBlank(request.getPolicyValue())) {
            throw new IllegalArgumentException("policyValue is required");
        }

        String adminId = request.getAdminId().trim();

        String normalizedKey = policyKey.trim().toUpperCase();
        if (!ALLOWED_KEYS.contains(normalizedKey)) {
            throw new IllegalArgumentException("unsupported policyKey");
        }

        SystemPolicy policy = repository.findByPolicyKey(normalizedKey).orElseGet(SystemPolicy::new);
        boolean created = policy.getId() == null;

        policy.setPolicyKey(normalizedKey);
        policy.setPolicyValue(request.getPolicyValue());
        policy.setUpdatedByAdminId(adminId);
        policy.setUpdatedAt(Instant.now());

        SystemPolicy saved = repository.save(policy);
        PolicyResponseDto response = new PolicyResponseDto(
            saved.getPolicyKey(),
            saved.getPolicyValue(),
            saved.getUpdatedByAdminId(),
            saved.getUpdatedAt()
        );

        return new PolicyUpsertResult(created, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
