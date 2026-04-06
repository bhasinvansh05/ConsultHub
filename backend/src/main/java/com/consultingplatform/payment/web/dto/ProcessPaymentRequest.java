package com.consultingplatform.payment.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    @NotNull(message = "clientId is required")
    private Long clientId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    private Long savedPaymentMethodId;

    private PaymentMethodDto paymentDetails;
}
