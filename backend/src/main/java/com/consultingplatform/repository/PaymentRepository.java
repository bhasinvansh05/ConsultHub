package com.consultingplatform.repository;

import com.consultingplatform.model.payment.Payment;
import com.consultingplatform.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByClientIdOrderByTimestampDesc(Long clientId);

    List<Payment> findByClientIdAndStatusOrderByTimestampDesc(Long clientId, PaymentStatus status);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByBookingId(Long bookingId);
}
