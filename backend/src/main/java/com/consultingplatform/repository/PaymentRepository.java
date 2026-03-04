package com.consultingplatform.repository;

import com.consultingplatform.model.payment.Payment;
import com.consultingplatform.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UC5 – Process Payment / UC7 – View Payment History
 * Primary key is now a String UUID to match the class diagram (id: String).
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /** UC7 – full payment history for a client, newest first */
    List<Payment> findByClientIdOrderByTimestampDesc(Long clientId);

    /** UC7 – history filtered by status (PENDING | COMPLETED | FAILED | REFUNDED) */
    List<Payment> findByClientIdAndStatusOrderByTimestampDesc(Long clientId, PaymentStatus status);

    /** Look up a single payment by its generated transaction ID */
    Optional<Payment> findByTransactionId(String transactionId);

    /** All payments tied to a specific booking */
    List<Payment> findByBookingId(Long bookingId);
}
