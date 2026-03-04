package com.consultingplatform.dto;

import lombok.*;

/**
 * UC6 – Manage Payment Methods
 * Input DTO for adding or updating a saved payment method.
 *
 * Sensitive fields (cardNumber, cvv, accountNumber) are accepted for
 * validation and masking only — they are never written to the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDto {

    /** PaymentType enum name: CREDIT_CARD | DEBIT_CARD | PAYPAL | BANK_TRANSFER */
    private String type;

    /** Whether this should become the client's default payment method */
    private Boolean isDefault;

    // --- Credit / Debit card fields (input only, validated then discarded) ---
    /** Full 16-digit card number – validated, last 4 stored, rest discarded */
    private String cardNumber;
    /** Expiry date in MM/YY format */
    private String expiryDate;
    /** 3-4 digit CVV – validated only, never stored */
    private String cvv;
    private String cardholderName;

    // --- PayPal field ---
    private String paypalEmail;

    // --- Bank transfer fields (input only, validated then discarded) ---
    /** Full account number (8–17 digits) – validated, last 4 stored, rest discarded */
    private String accountNumber;
    /** 9-digit ABA routing number */
    private String routingNumber;
}
