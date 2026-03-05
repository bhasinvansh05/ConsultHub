package com.consultingplatform.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.admin.web.dto.ConsultantApprovalDecision;
import com.consultingplatform.admin.web.dto.ConsultantApprovalRequestDto;
import com.consultingplatform.admin.web.dto.ConsultantApprovalResponseDto;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultantApprovalServiceImplTest {

    @Mock
    private ConsultantRegistrationRepository repository;

    @InjectMocks
    private ConsultantApprovalServiceImpl service;

    @Test
    void approvePendingConsultant_success() {
        ConsultantRegistration registration = new ConsultantRegistration();
        registration.setConsultantId(1001L);
        registration.setStatus(ConsultantApprovalStatus.PENDING);
        registration.setCreatedAt(Instant.now());

        ConsultantApprovalRequestDto request = new ConsultantApprovalRequestDto();
        request.setAdminId("admin-1");
        request.setDecision(ConsultantApprovalDecision.APPROVE);

        when(repository.findByConsultantId(1001L)).thenReturn(Optional.of(registration));
        when(repository.save(any(ConsultantRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultantApprovalResponseDto response = service.approveOrRejectConsultant(1001L, request);

        assertEquals(ConsultantApprovalStatus.APPROVED, response.getNewStatus());
        assertEquals("admin-1", response.getApprovedByAdminId());
        assertNotNull(response.getDecidedAt());
        verify(repository).save(any(ConsultantRegistration.class));
    }

    @Test
    void rejectPendingConsultant_success() {
        ConsultantRegistration registration = new ConsultantRegistration();
        registration.setConsultantId(1001L);
        registration.setStatus(ConsultantApprovalStatus.PENDING);
        registration.setCreatedAt(Instant.now());

        ConsultantApprovalRequestDto request = new ConsultantApprovalRequestDto();
        request.setAdminId("admin-1");
        request.setDecision(ConsultantApprovalDecision.REJECT);

        when(repository.findByConsultantId(1001L)).thenReturn(Optional.of(registration));
        when(repository.save(any(ConsultantRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultantApprovalResponseDto response = service.approveOrRejectConsultant(1001L, request);

        assertEquals(ConsultantApprovalStatus.REJECTED, response.getNewStatus());
        assertEquals("admin-1", response.getApprovedByAdminId());
        assertNotNull(response.getDecidedAt());
    }

    @Test
    void consultantNotFound_failsWithNotFound() {
        ConsultantApprovalRequestDto request = new ConsultantApprovalRequestDto();
        request.setAdminId("admin-1");
        request.setDecision(ConsultantApprovalDecision.APPROVE);

        when(repository.findByConsultantId(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.approveOrRejectConsultant(404L, request));
    }

    @Test
    void missingDecision_failsValidation() {
        ConsultantApprovalRequestDto request = new ConsultantApprovalRequestDto();
        request.setAdminId("admin-1");

        assertThrows(IllegalArgumentException.class, () -> service.approveOrRejectConsultant(1001L, request));
    }
}
