package com.consultingplatform.model.payment;

import com.consultingplatform.model.enums.PaymentStatus;
import com.consultingplatform.model.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Concrete strategy for bank transfer payments.
 * Fields from class diagram: accountNumber, routingNumber
 * Method from class diagram: validateBankDetails()
 */
@Data
@AllArgsConstructor
public class BankTransferPayment implements PaymentStrategy {

    private String accountNumber;
    private String routingNumber;

    // Checks account number (8-17 digits) and routing number (exactly 9 digits)
    public boolean validateBankDetails() {
        if (accountNumber == null || routingNumber == null) {
            return false;
        }

        // Remove spaces before checking
        String cleanAcc = accountNumber.replaceAll(" ", "");
        if (!cleanAcc.matches("\\d{8,17}")) {
            return false;
        }

        String cleanRouting = routingNumber.replaceAll(" ", "");
        return cleanRouting.matches("\\d{9}");
    }

    @Override
    public boolean validatePaymentDetails() {
        return validateBankDetails();
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
        return PaymentType.BANK_TRANSFER.name();
    }
}
