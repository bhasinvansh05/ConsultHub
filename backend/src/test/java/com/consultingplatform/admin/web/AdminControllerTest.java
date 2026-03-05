package com.consultingplatform.admin.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.service.ConflictException;
import com.consultingplatform.admin.service.ConsultantApprovalService;
import com.consultingplatform.admin.service.PolicyUpsertResult;
import com.consultingplatform.admin.service.SystemPolicyService;
import com.consultingplatform.admin.web.dto.ConsultantApprovalResponseDto;
import com.consultingplatform.admin.web.dto.PolicyResponseDto;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminControllerTest {

    private MockMvc mockMvc;
    private ConsultantApprovalService consultantApprovalService;
    private SystemPolicyService systemPolicyService;
    private ConsultantRegistrationRepository consultantRegistrationRepository;

    @BeforeEach
    void setup() {
        consultantApprovalService = Mockito.mock(ConsultantApprovalService.class);
        systemPolicyService = Mockito.mock(SystemPolicyService.class);
        consultantRegistrationRepository = Mockito.mock(ConsultantRegistrationRepository.class);

        AdminController controller = new AdminController(
            consultantApprovalService,
            systemPolicyService,
            consultantRegistrationRepository
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void approveConsultant_success() throws Exception {
        ConsultantApprovalResponseDto response = new ConsultantApprovalResponseDto(
            1001L,
            ConsultantApprovalStatus.APPROVED,
            "admin-1",
            Instant.now()
        );
        when(consultantApprovalService.approveOrRejectConsultant(eq(1001L), any())).thenReturn(response);

        String body = "{\"adminId\":\"admin-1\",\"decision\":\"APPROVE\",\"reason\":\"ok\"}";

        mockMvc.perform(post("/api/admin/consultants/1001/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consultantId").value(1001))
            .andExpect(jsonPath("$.newStatus").value("APPROVED"));
    }

    @Test
    void reapproveConsultant_conflict() throws Exception {
        when(consultantApprovalService.approveOrRejectConsultant(eq(1001L), any()))
            .thenThrow(new ConflictException("Consultant registration already decided"));

        String body = "{\"adminId\":\"admin-1\",\"decision\":\"APPROVE\",\"reason\":\"\"}";

        mockMvc.perform(post("/api/admin/consultants/1001/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void createPolicy_success201() throws Exception {
        PolicyResponseDto response = new PolicyResponseDto("CANCELLATION_RULES", "strict", "admin-2", Instant.now());
        when(systemPolicyService.upsertPolicy(eq("CANCELLATION_RULES"), any()))
            .thenReturn(new PolicyUpsertResult(true, response));

        String body = "{\"adminId\":\"admin-2\",\"policyValue\":\"strict\"}";

        mockMvc.perform(put("/api/admin/policies/CANCELLATION_RULES")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.policyKey").value("CANCELLATION_RULES"));
    }

    @Test
    void missingAdminId_validationError400() throws Exception {
        String body = "{\"adminId\":\"\",\"policyValue\":\"standard\"}";

        mockMvc.perform(put("/api/admin/policies/REFUND_POLICY")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.details.adminId").value("adminId is required"));
    }

    @Test
    void missingDecision_validationError400() throws Exception {
        String body = "{\"adminId\":\"admin-1\"}";

        mockMvc.perform(post("/api/admin/consultants/1001/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.decision").value("decision is required"));
    }

}
