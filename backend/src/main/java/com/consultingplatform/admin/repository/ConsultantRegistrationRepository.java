package com.consultingplatform.admin.repository;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultantRegistrationRepository extends JpaRepository<ConsultantRegistration, Long> {
    Optional<ConsultantRegistration> findByConsultantId(Long consultantId);
    List<ConsultantRegistration> findByStatus(ConsultantApprovalStatus status);
}
