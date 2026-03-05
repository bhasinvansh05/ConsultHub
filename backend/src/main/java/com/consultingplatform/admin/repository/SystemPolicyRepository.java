package com.consultingplatform.admin.repository;

import com.consultingplatform.admin.domain.SystemPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemPolicyRepository extends JpaRepository<SystemPolicy, Long> {
    Optional<SystemPolicy> findByPolicyKey(String policyKey);
}
