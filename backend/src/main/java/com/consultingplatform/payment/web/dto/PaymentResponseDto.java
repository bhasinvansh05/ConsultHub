package com.consultingplatform.payment.web.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private String id;
    private String transactionId;
    private Long bookingId;
    private Long clientId;
    private BigDecimal amount;
    private String status;
    private String paymentType;
    private LocalDateTime timestamp;
    private String failureReason;
    private BigDecimal refundAmount;
    private LocalDateTime refundedAt;
}
