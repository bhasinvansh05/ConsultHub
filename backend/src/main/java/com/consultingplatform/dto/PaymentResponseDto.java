package com.consultingplatform.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UC5 – Process Payment / UC7 – View Payment History
 * Read-only response payload for a single payment transaction.
 * Mirrors the Payment class diagram fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    /** UUID string – matches diagram: id: String */
    private String id;

    private String transactionId;
    private Long bookingId;
    private Long clientId;
    private BigDecimal amount;

    /** PaymentStatus name: PENDING | COMPLETED | FAILED | REFUNDED */
    private String status;

    /** strategy.getPaymentType(): CREDIT_CARD | DEBIT_CARD | PAYPAL | BANK_TRANSFER */
    private String paymentType;

    /** Matches diagram: timestamp: DateTime */
    private LocalDateTime timestamp;

    /** Populated only when status is FAILED */
    private String failureReason;
}
