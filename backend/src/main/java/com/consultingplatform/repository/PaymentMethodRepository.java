package com.consultingplatform.repository;

import com.consultingplatform.model.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UC6 - Manage Payment Methods
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /** Returns all saved methods for a client. */
    List<PaymentMethod> findByClientId(Long clientId);

    /** Returns a saved method only if owned by the specified client. */
    Optional<PaymentMethod> findByIdAndClientId(Long id, Long clientId);

    /** Used by removePaymentMethod - must be called inside a @Transactional context. */
    void deleteByIdAndClientId(Long id, Long clientId);
}
