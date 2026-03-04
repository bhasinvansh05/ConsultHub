package com.consultingplatform.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * UC5 – Process Payment
 * Request body for initiating a payment against a confirmed booking.
 *
 * The client may either:
 *   (a) reference an existing saved payment method via savedPaymentMethodId, or
 *   (b) supply payment details inline via paymentDetails (one-time use, not saved).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    private Long bookingId;
    private Long clientId;
    private BigDecimal amount;

    /** ID of an existing PaymentMethod row owned by this client (optional) */
    private Long savedPaymentMethodId;

    /** Inline payment details used when no saved method is selected (optional) */
    private PaymentMethodDto paymentDetails;
}
