package com.consultingplatform.payment.web.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    private Long bookingId;
    private Long clientId;
    private BigDecimal amount;
    private Long savedPaymentMethodId;
    private PaymentMethodDto paymentDetails;
}
