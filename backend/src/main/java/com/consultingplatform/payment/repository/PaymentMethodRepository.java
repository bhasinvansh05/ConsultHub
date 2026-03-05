package com.consultingplatform.payment.repository;

import com.consultingplatform.payment.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByClientId(Long clientId);

    Optional<PaymentMethod> findByIdAndClientId(Long id, Long clientId);

    void deleteByIdAndClientId(Long id, Long clientId);
}
