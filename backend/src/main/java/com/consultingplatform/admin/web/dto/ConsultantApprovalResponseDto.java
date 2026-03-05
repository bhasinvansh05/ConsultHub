package com.consultingplatform.admin.web.dto;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import java.time.Instant;

public class ConsultantApprovalResponseDto {
    private Long consultantId;
    private ConsultantApprovalStatus newStatus;
    private String approvedByAdminId;
    private Instant decidedAt;

    public ConsultantApprovalResponseDto(Long consultantId, ConsultantApprovalStatus newStatus, String approvedByAdminId, Instant decidedAt) {
        this.consultantId = consultantId;
        this.newStatus = newStatus;
        this.approvedByAdminId = approvedByAdminId;
        this.decidedAt = decidedAt;
    }

    public Long getConsultantId() {
        return consultantId;
    }

    public ConsultantApprovalStatus getNewStatus() {
        return newStatus;
    }

    public String getApprovedByAdminId() {
        return approvedByAdminId;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
