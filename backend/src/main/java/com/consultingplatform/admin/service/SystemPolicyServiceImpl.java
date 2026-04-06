package com.consultingplatform.admin.service;

import com.consultingplatform.admin.domain.SystemPolicy;
import com.consultingplatform.admin.repository.SystemPolicyRepository;
import com.consultingplatform.admin.web.dto.PolicyResponseDto;
import com.consultingplatform.admin.web.dto.PolicyUpsertRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SystemPolicyServiceImpl implements SystemPolicyService {
    private static final Logger log = LoggerFactory.getLogger(SystemPolicyServiceImpl.class);

    private static final Set<String> ALLOWED_KEYS = Set.of(
        "PRICING_STRATEGY",
        "NOTIFICATION_SETTINGS",
        "REFUND_POLICY"
    );

    private final SystemPolicyRepository repository;
    private final ObjectMapper objectMapper;

    public SystemPolicyServiceImpl(SystemPolicyRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> getPolicyConfig(String policyKey, Class<T> configClass) {
        if (isBlank(policyKey)) {
            return Optional.empty();
        }
        
        String normalizedKey = policyKey.trim().toUpperCase();
        Optional<SystemPolicy> policyOpt = repository.findByPolicyKey(normalizedKey);
        
        if (policyOpt.isEmpty() || isBlank(policyOpt.get().getPolicyValue())) {
            return Optional.empty();
        }
        
        try {
            T config = objectMapper.readValue(policyOpt.get().getPolicyValue(), configClass);
            return Optional.ofNullable(config);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse policy JSON for key: {}", normalizedKey, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PolicyResponseDto> getPolicy(String policyKey) {
        if (isBlank(policyKey)) return Optional.empty();
        String normalizedKey = policyKey.trim().toUpperCase();
        return repository.findByPolicyKey(normalizedKey).map(saved -> new PolicyResponseDto(
            saved.getPolicyKey(),
            saved.getPolicyValue(),
            saved.getUpdatedByAdminId(),
            saved.getUpdatedAt()
        ));
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
