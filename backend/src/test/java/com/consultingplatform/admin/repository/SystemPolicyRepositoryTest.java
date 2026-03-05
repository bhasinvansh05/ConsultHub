package com.consultingplatform.admin.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consultingplatform.admin.domain.SystemPolicy;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class SystemPolicyRepositoryTest {

    @Autowired
    private SystemPolicyRepository repository;

    @Test
    void findByPolicyKey_returnsPersistedPolicy() {
        SystemPolicy policy = new SystemPolicy();
        policy.setPolicyKey("CANCELLATION_RULES");
        policy.setPolicyValue("strict");
        policy.setUpdatedByAdminId("admin-1");
        policy.setUpdatedAt(Instant.now());

        repository.saveAndFlush(policy);

        assertTrue(repository.findByPolicyKey("CANCELLATION_RULES").isPresent());
    }

    @Test
    void uniquePolicyKey_enforced() {
        SystemPolicy first = new SystemPolicy();
        first.setPolicyKey("REFUND_POLICY");
        first.setPolicyValue("standard");
        first.setUpdatedByAdminId("admin-1");
        first.setUpdatedAt(Instant.now());
        repository.saveAndFlush(first);

        SystemPolicy duplicate = new SystemPolicy();
        duplicate.setPolicyKey("REFUND_POLICY");
        duplicate.setPolicyValue("strict");
        duplicate.setUpdatedByAdminId("admin-2");
        duplicate.setUpdatedAt(Instant.now());

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }

    @Test
    void persistedValues_roundTrip() {
        SystemPolicy policy = new SystemPolicy();
        policy.setPolicyKey("NOTIFICATION_SETTINGS");
        policy.setPolicyValue("email-only");
        policy.setUpdatedByAdminId("admin-2");
        policy.setUpdatedAt(Instant.now());

        SystemPolicy saved = repository.saveAndFlush(policy);
        SystemPolicy loaded = repository.findById(saved.getId()).orElseThrow();

        assertEquals("NOTIFICATION_SETTINGS", loaded.getPolicyKey());
        assertEquals("email-only", loaded.getPolicyValue());
        assertEquals("admin-2", loaded.getUpdatedByAdminId());
    }
}
