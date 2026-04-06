package com.consultingplatform.admin.service;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.user.domain.User;
import com.consultingplatform.user.repository.UserRepository;
import com.consultingplatform.admin.web.dto.ConsultantApprovalDecision;
import com.consultingplatform.admin.web.dto.ConsultantApprovalRequestDto;
import com.consultingplatform.admin.web.dto.ConsultantApprovalResponseDto;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import com.consultingplatform.security.CustomUserDetails;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultantApprovalServiceImpl implements ConsultantApprovalService {

    private final ConsultantRegistrationRepository repository;
    private final UserRepository userRepository;

    public ConsultantApprovalServiceImpl(ConsultantRegistrationRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
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
        
        // Also update the User accountStatus
        User consultantUser = userRepository.findById(consultantId)
            .orElseThrow(() -> new ResourceNotFoundException("Consultant user not found"));
            
        if (newStatus == ConsultantApprovalStatus.APPROVED) {
            consultantUser.setAccountStatus("ACTIVE");
        } else {
            consultantUser.setAccountStatus("INACTIVE");
        }
        userRepository.save(consultantUser);

        // Record the approving admin from the authenticated principal rather than trusting the request
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Long adminId = ((CustomUserDetails) principal).getId();
            registration.setApprovedByAdminId(adminId == null ? null : adminId.toString());
        }

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
