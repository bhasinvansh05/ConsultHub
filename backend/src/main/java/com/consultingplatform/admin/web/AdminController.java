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
import com.consultingplatform.consultingservice.domain.ConsultingService;
import com.consultingplatform.consultingservice.service.ConsultingServiceService;
import com.consultingplatform.user.domain.Admin;
import com.consultingplatform.user.repository.UserRepository;
import com.consultingplatform.consultingservice.web.dto.ConsultingServiceDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.consultingplatform.admin.web.dto.SystemStatusStubDto;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ConsultantApprovalService consultantApprovalService;
    private final SystemPolicyService systemPolicyService;
    private final ConsultantRegistrationRepository consultantRegistrationRepository;
    private final UserRepository userRepository;
    private final ConsultingServiceService consultingServiceService;
    private final com.consultingplatform.booking.repository.BookingRepository bookingRepository;

    public AdminController(
        ConsultantApprovalService consultantApprovalService,
        SystemPolicyService systemPolicyService,
        ConsultantRegistrationRepository consultantRegistrationRepository,
        UserRepository userRepository,
        ConsultingServiceService consultingServiceService,
        com.consultingplatform.booking.repository.BookingRepository bookingRepository
    ) {
        this.consultantApprovalService = consultantApprovalService;
        this.systemPolicyService = systemPolicyService;
        this.consultantRegistrationRepository = consultantRegistrationRepository;
        this.userRepository = userRepository;
        this.consultingServiceService = consultingServiceService;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/services")
    public ResponseEntity<List<ConsultingService>> listConsultingServicesForAdmin() {
        return ResponseEntity.ok(consultingServiceService.getAllServicesForAdmin());
    }

    @PostMapping("/services")
    public ResponseEntity<ConsultingService> createConsultingService(@Valid @RequestBody ConsultingServiceDto serviceDto) {
        ConsultingService createdService = consultingServiceService.createService(serviceDto);

        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<ConsultingService> updateConsultingService(
            @PathVariable Long id,
            @Valid @RequestBody ConsultingServiceDto serviceDto) {
        return ResponseEntity.ok(consultingServiceService.updateService(id, serviceDto));
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteConsultingService(@PathVariable Long id) {
        consultingServiceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/consultants/{consultantId}/approval")
    public ResponseEntity<ConsultantApprovalResponseDto> approveOrRejectConsultant(
        @PathVariable Long consultantId,
        @Valid @RequestBody ConsultantApprovalRequestDto request
    ) {
        // Admin identity and role are enforced at the service layer; ignore any client-supplied adminId
        return ResponseEntity.ok(consultantApprovalService.approveOrRejectConsultant(consultantId, request));
    }

    @GetMapping("/consultants/pending")
    public ResponseEntity<List<ConsultantRegistration>> getPendingConsultantRegistrations() {
        return ResponseEntity.ok(consultantRegistrationRepository.findByStatus(ConsultantApprovalStatus.PENDING));
    }

    @GetMapping("/policies/{policyKey}")
    public ResponseEntity<PolicyResponseDto> getPolicy(@PathVariable String policyKey) {
        return systemPolicyService.getPolicy(policyKey)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
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

    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getAdminStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalConsultants", userRepository.countByRole("CONSULTANT"));
        stats.put("totalClients", userRepository.countByRole("CLIENT"));
        
        stats.put("bookedAppointments", bookingRepository.countByStatus("PAID") + bookingRepository.countByStatus("COMPLETED"));
        stats.put("appointmentsRequested", bookingRepository.countByStatus("REQUESTED"));
        stats.put("appointmentsWaitingPayment", bookingRepository.countByStatus("CONFIRMED"));
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/system/status")
    public ResponseEntity<SystemStatusStubDto> getSystemStatus() {
        return ResponseEntity.ok(
            new SystemStatusStubDto("UP", true, "Application is running.")
        );
    }
}
