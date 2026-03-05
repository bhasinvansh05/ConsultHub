package com.consultingplatform.consultant.repository;

import com.consultingplatform.consultant.domain.ConsultingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultingServiceRepository extends JpaRepository<ConsultingService, Long> {
    
    List<ConsultingService> findByIsActiveTrue();
    
    List<ConsultingService> findByConsultantIdAndIsActiveTrue(Long consultantId);
    
    List<ConsultingService> findByServiceTypeAndIsActiveTrue(String serviceType);

    boolean existsByIdAndConsultantIdAndIsActiveTrue(Long id, Long consultantId);

    Optional<ConsultingService> findByIdAndConsultantIdAndIsActiveTrue(Long id, Long consultantId);
}
