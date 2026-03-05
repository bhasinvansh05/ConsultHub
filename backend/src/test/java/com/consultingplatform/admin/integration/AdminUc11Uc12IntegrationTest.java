package com.consultingplatform.admin.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.consultingplatform.ConsultiingPlatform;
import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.admin.repository.SystemPolicyRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ConsultiingPlatform.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUc11Uc12IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsultantRegistrationRepository consultantRegistrationRepository;

    @Autowired
    private SystemPolicyRepository systemPolicyRepository;

    @BeforeEach
    void setupData() {
        systemPolicyRepository.deleteAll();
        consultantRegistrationRepository.deleteAll();
    }

    @Test
    void uc11_approveConsultant_success() throws Exception {
        createPendingConsultant(3001L);

        String body = "{\"adminId\":\"admin-1\",\"decision\":\"APPROVE\",\"reason\":\"verified\"}";

        mockMvc.perform(post("/api/admin/consultants/3001/approval")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consultantId").value(3001))
            .andExpect(jsonPath("$.newStatus").value("APPROVED"));
    }

    @Test
    void uc11_rejectConsultant_success() throws Exception {
        createPendingConsultant(3002L);
        String body = "{\"adminId\":\"admin-1\",\"decision\":\"REJECT\",\"reason\":\"incomplete profile\"}";

        mockMvc.perform(post("/api/admin/consultants/3002/approval")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consultantId").value(3002))
            .andExpect(jsonPath("$.newStatus").value("REJECTED"));
    }

    @Test
    void uc11_getPendingConsultants_returnsPendingList() throws Exception {
        createPendingConsultant(3004L);

        mockMvc.perform(get("/api/admin/consultants/pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].consultantId").value(3004))
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void uc11_missingAdminId_validationError() throws Exception {
        createPendingConsultant(3003L);
        String body = "{\"adminId\":\"\",\"decision\":\"REJECT\"}";

        mockMvc.perform(post("/api/admin/consultants/3003/approval")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.adminId").value("adminId is required"));
    }

    @Test
    void uc12_createPolicy_success() throws Exception {
        String body = "{\"adminId\":\"admin-1\",\"policyValue\":\"strict\"}";

        mockMvc.perform(put("/api/admin/policies/CANCELLATION_RULES")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.policyKey").value("CANCELLATION_RULES"));
    }

    @Test
    void uc12_updatePolicy_success() throws Exception {
        String createBody = "{\"adminId\":\"admin-1\",\"policyValue\":\"strict\"}";
        String updateBody = "{\"adminId\":\"admin-1\",\"policyValue\":\"moderate\"}";

        mockMvc.perform(put("/api/admin/policies/REFUND_POLICY")
                .contentType("application/json")
                .content(createBody))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/admin/policies/REFUND_POLICY")
                .contentType("application/json")
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.policyValue").value("moderate"));
    }

    @Test
    void uc12_invalidPolicyKey_validationError() throws Exception {
        String body = "{\"adminId\":\"admin-1\",\"policyValue\":\"x\"}";

        mockMvc.perform(put("/api/admin/policies/UNKNOWN_KEY")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("unsupported policyKey"));
    }

    @Test
    void uc12_missingAdminId_validationError() throws Exception {
        String body = "{\"adminId\":\"\",\"policyValue\":\"x\"}";

        mockMvc.perform(put("/api/admin/policies/PRICING_STRATEGY")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.adminId").value("adminId is required"));
    }

    private void createPendingConsultant(Long consultantId) {
        ConsultantRegistration registration = new ConsultantRegistration();
        registration.setConsultantId(consultantId);
        registration.setStatus(ConsultantApprovalStatus.PENDING);
        registration.setCreatedAt(Instant.now());
        consultantRegistrationRepository.save(registration);
    }
}
