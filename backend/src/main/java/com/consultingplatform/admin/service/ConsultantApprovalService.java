package com.consultingplatform.admin.service;

import com.consultingplatform.admin.web.dto.ConsultantApprovalRequestDto;
import com.consultingplatform.admin.web.dto.ConsultantApprovalResponseDto;

public interface ConsultantApprovalService {
    ConsultantApprovalResponseDto approveOrRejectConsultant(Long consultantId, ConsultantApprovalRequestDto request);
}
