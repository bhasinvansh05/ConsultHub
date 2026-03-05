package com.consultingplatform.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DebitCardPayment implements PaymentStrategy {

    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public boolean validateCard() {
        if (cardNumber == null || cvv == null || expiryDate == null) {
            return false;
        }
        String cleanCard = cardNumber.replaceAll(" ", "");
        if (!cleanCard.matches("\\d{16}")) {
            return false;
        }
        String cleanCvv = cvv.trim();
        if (!cleanCvv.matches("\\d{3,4}")) {
            return false;
        }
        return isNotExpired(expiryDate);
    }

    private boolean isNotExpired(String expiry) {
        if (expiry == null || !expiry.contains("/")) {
            return false;
        }
        String[] parts = expiry.trim().split("/");
        if (parts.length != 2) {
            return false;
        }
        try {
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);
            if (month < 1 || month > 12) {
                return false;
            }
            LocalDate now = LocalDate.now();
            return year > now.getYear() || (year == now.getYear() && month >= now.getMonthValue());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean validatePaymentDetails() {
        return validateCard();
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
        return PaymentType.DEBIT_CARD.name();
    }
}
