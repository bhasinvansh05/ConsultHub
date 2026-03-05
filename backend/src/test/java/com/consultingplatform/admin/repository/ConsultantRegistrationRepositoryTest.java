package com.consultingplatform.admin.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ConsultantRegistrationRepositoryTest {

    @Autowired
    private ConsultantRegistrationRepository repository;

    @Test
    void findByConsultantId_returnsPersistedEntity() {
        ConsultantRegistration registration = new ConsultantRegistration();
        registration.setConsultantId(2001L);
        registration.setStatus(ConsultantApprovalStatus.PENDING);
        registration.setCreatedAt(Instant.now());

        repository.saveAndFlush(registration);

        assertTrue(repository.findByConsultantId(2001L).isPresent());
    }

    @Test
    void uniqueConsultantId_enforced() {
        ConsultantRegistration first = new ConsultantRegistration();
        first.setConsultantId(2002L);
        first.setStatus(ConsultantApprovalStatus.PENDING);
        first.setCreatedAt(Instant.now());
        repository.saveAndFlush(first);

        ConsultantRegistration duplicate = new ConsultantRegistration();
        duplicate.setConsultantId(2002L);
        duplicate.setStatus(ConsultantApprovalStatus.PENDING);
        duplicate.setCreatedAt(Instant.now());

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }

    @Test
    void enumAndTimestamp_roundTripPersistence() {
        ConsultantRegistration registration = new ConsultantRegistration();
        registration.setConsultantId(2003L);
        registration.setStatus(ConsultantApprovalStatus.REJECTED);
        registration.setCreatedAt(Instant.now());
        registration.setDecidedAt(Instant.now());
        registration.setApprovedByAdminId("admin-1");

        ConsultantRegistration saved = repository.saveAndFlush(registration);

        ConsultantRegistration loaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(ConsultantApprovalStatus.REJECTED, loaded.getStatus());
        assertEquals("admin-1", loaded.getApprovedByAdminId());
    }
}
