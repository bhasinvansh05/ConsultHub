package com.consultingplatform.admin.web;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.admin.service.ConsultantApprovalService;
import com.consultingplatform.admin.service.PolicyUpsertResult;
import com.consultingplatform.admin.service.SystemPolicyService;
import com.consultingplatform.admin.web.dto.ConsultantApprovalRequestDto;
import com.consultingplatform.admin.web.dto.ConsultantApprovalResponseDto;
import com.consultingplatform.admin.web.dto.PolicyResponseDto;
import com.consultingplatform.admin.web.dto.PolicyUpsertRequestDto;
import com.consultingplatform.user.domain.Admin;
import com.consultingplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ConsultantApprovalService consultantApprovalService;
    private final SystemPolicyService systemPolicyService;
    private final ConsultantRegistrationRepository consultantRegistrationRepository;
    private final UserRepository userRepository;

    public AdminController(
        ConsultantApprovalService consultantApprovalService,
        SystemPolicyService systemPolicyService,
        ConsultantRegistrationRepository consultantRegistrationRepository,
        UserRepository userRepository
    ) {
        this.consultantApprovalService = consultantApprovalService;
        this.systemPolicyService = systemPolicyService;
        this.consultantRegistrationRepository = consultantRegistrationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/consultants/{consultantId}/approval")
    public ResponseEntity<ConsultantApprovalResponseDto> approveOrRejectConsultant(
        @PathVariable Long consultantId,
        @Valid @RequestBody ConsultantApprovalRequestDto request
    ) {
        String adminId = request.getAdminId();
        Long adminUserId;
        try {
            adminUserId = Long.parseLong(adminId.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can approve or reject consultant registration");
        }

        boolean isAdmin = userRepository.findById(adminUserId)
            .map(user -> user instanceof Admin)
            .orElse(false);
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can approve or reject consultant registration");
        }

        return ResponseEntity.ok(consultantApprovalService.approveOrRejectConsultant(consultantId, request));
    }

    @GetMapping("/consultants/pending")
    public ResponseEntity<List<ConsultantRegistration>> getPendingConsultantRegistrations() {
        return ResponseEntity.ok(consultantRegistrationRepository.findByStatus(ConsultantApprovalStatus.PENDING));
    }

    @PutMapping("/policies/{policyKey}")
    public ResponseEntity<PolicyResponseDto> upsertPolicy(
        @PathVariable String policyKey,
        @Valid @RequestBody PolicyUpsertRequestDto request
    ) {
        PolicyUpsertResult result = systemPolicyService.upsertPolicy(policyKey, request);
        HttpStatus status = result.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.getResponse());
    }
}
