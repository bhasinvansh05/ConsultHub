package com.consultingplatform.model.payment;

import com.consultingplatform.model.enums.PaymentStatus;
import com.consultingplatform.model.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Concrete strategy for credit card payments.
 * Fields from class diagram: cardNumber, expiryDate, cvv
 * Method from class diagram: validateCard()
 */
@Data
@AllArgsConstructor
public class CreditCardPayment implements PaymentStrategy {

    private String cardNumber;
    private String expiryDate;  // format: MM/YY
    private String cvv;

    // Checks card number (16 digits), expiry (not past), CVV (3-4 digits)
    public boolean validateCard() {
        if (cardNumber == null || cvv == null || expiryDate == null) {
            return false;
        }

        // Remove spaces and check for 16 digits
        String cleanCard = cardNumber.replaceAll(" ", "");
        if (!cleanCard.matches("\\d{16}")) {
            return false;
        }

        // CVV must be 3 or 4 digits
        String cleanCvv = cvv.trim();
        if (!cleanCvv.matches("\\d{3,4}")) {
            return false;
        }

        return isNotExpired(expiryDate);
    }

    // Returns true if the card is still valid (month/year is current or future)
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
            // Valid if the expiry year is in the future, or same year but month hasn't passed
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
        // Generate a unique transaction ID
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
        return PaymentType.CREDIT_CARD.name();
    }
}
