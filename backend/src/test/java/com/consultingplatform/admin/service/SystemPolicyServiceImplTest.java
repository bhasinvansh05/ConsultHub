package com.consultingplatform.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.consultingplatform.admin.domain.SystemPolicy;
import com.consultingplatform.admin.repository.SystemPolicyRepository;
import com.consultingplatform.admin.web.dto.PolicyUpsertRequestDto;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemPolicyServiceImplTest {

    @Mock
    private SystemPolicyRepository repository;

    @InjectMocks
    private SystemPolicyServiceImpl service;

    @Test
    void createNewPolicy_success() {
        PolicyUpsertRequestDto request = new PolicyUpsertRequestDto();
        request.setAdminId("admin-1");
        request.setPolicyValue("strict");

        when(repository.findByPolicyKey("CANCELLATION_RULES")).thenReturn(Optional.empty());
        when(repository.save(any(SystemPolicy.class))).thenAnswer(invocation -> {
            SystemPolicy policy = invocation.getArgument(0);
            policy.setId(1L);
            return policy;
        });

        PolicyUpsertResult result = service.upsertPolicy("CANCELLATION_RULES", request);

        assertEquals(true, result.isCreated());
        assertEquals("CANCELLATION_RULES", result.getResponse().getPolicyKey());
        assertEquals("strict", result.getResponse().getPolicyValue());
    }

    @Test
    void updateExistingPolicy_success() {
        PolicyUpsertRequestDto request = new PolicyUpsertRequestDto();
        request.setAdminId("admin-2");
        request.setPolicyValue("dynamic");

        SystemPolicy existing = new SystemPolicy();
        existing.setId(11L);
        existing.setPolicyKey("PRICING_STRATEGY");
        existing.setPolicyValue("fixed");

        when(repository.findByPolicyKey("PRICING_STRATEGY")).thenReturn(Optional.of(existing));
        when(repository.save(any(SystemPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyUpsertResult result = service.upsertPolicy("pricing_strategy", request);

        assertEquals(false, result.isCreated());
        assertEquals("PRICING_STRATEGY", result.getResponse().getPolicyKey());
        assertEquals("dynamic", result.getResponse().getPolicyValue());
    }

    @Test
    void missingAdminId_validationError() {
        PolicyUpsertRequestDto request = new PolicyUpsertRequestDto();
        request.setPolicyValue("enabled");

        assertThrows(IllegalArgumentException.class, () -> service.upsertPolicy("NOTIFICATION_SETTINGS", request));
    }

    @Test
    void missingPolicyValue_validationError() {
        PolicyUpsertRequestDto request = new PolicyUpsertRequestDto();
        request.setAdminId("admin-1");

        assertThrows(IllegalArgumentException.class, () -> service.upsertPolicy("NOTIFICATION_SETTINGS", request));
    }

    @Test
    void unsupportedPolicyKey_validationError() {
        PolicyUpsertRequestDto request = new PolicyUpsertRequestDto();
        request.setAdminId("admin-1");
        request.setPolicyValue("x");

        assertThrows(IllegalArgumentException.class, () -> service.upsertPolicy("UNKNOWN_POLICY", request));
    }
}
