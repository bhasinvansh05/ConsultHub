package com.consultingplatform.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PayPalPayment implements PaymentStrategy {

    private String email;

    public boolean validateEmail() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String trimmed = email.trim();
        int atIndex = trimmed.indexOf("@");
        int lastDotIndex = trimmed.lastIndexOf(".");

        return atIndex > 0 && lastDotIndex > atIndex + 1 && lastDotIndex < trimmed.length() - 1;
    }

    @Override
    public boolean validatePaymentDetails() {
        return validateEmail();
    }

    @Override
    public Payment processPayment(double amount) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String txnId = "TXN-" + uuid.substring(0, 12).toUpperCase();

        Payment payment = new Payment();
        payment.setTransactionId(txnId);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setTimestamp(LocalDateTime.now());
        payment.setStrategyType(getPaymentType());
        payment.setStatus(PaymentStatus.COMPLETED);
        return payment;
    }

    @Override
    public String getPaymentType() {
        return PaymentType.PAYPAL.name();
    }
}
