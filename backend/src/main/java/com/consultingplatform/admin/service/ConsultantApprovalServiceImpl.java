package com.consultingplatform.admin.service;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.admin.web.dto.ConsultantApprovalDecision;
import com.consultingplatform.admin.web.dto.ConsultantApprovalRequestDto;
import com.consultingplatform.admin.web.dto.ConsultantApprovalResponseDto;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ConsultantApprovalServiceImpl implements ConsultantApprovalService {

    private final ConsultantRegistrationRepository repository;

    public ConsultantApprovalServiceImpl(ConsultantRegistrationRepository repository) {
        this.repository = repository;
    }

    @Override
    public ConsultantApprovalResponseDto approveOrRejectConsultant(Long consultantId, ConsultantApprovalRequestDto request) {
        if (request == null || request.getDecision() == null) {
            throw new IllegalArgumentException("decision is required");
        }

        ConsultantRegistration registration = repository.findByConsultantId(consultantId)
            .orElseThrow(() -> new ResourceNotFoundException("Consultant registration not found"));

        ConsultantApprovalStatus newStatus = request.getDecision() == ConsultantApprovalDecision.APPROVE
            ? ConsultantApprovalStatus.APPROVED
            : ConsultantApprovalStatus.REJECTED;

        registration.setStatus(newStatus);
        registration.setApprovedByAdminId(request.getAdminId());
        registration.setDecisionReason(request.getReason());
        registration.setDecidedAt(Instant.now());

        ConsultantRegistration saved = repository.save(registration);

        return new ConsultantApprovalResponseDto(
            saved.getConsultantId(),
            saved.getStatus(),
            saved.getApprovedByAdminId(),
            saved.getDecidedAt()
        );
    }
}
