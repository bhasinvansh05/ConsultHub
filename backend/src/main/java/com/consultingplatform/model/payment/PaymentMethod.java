package com.consultingplatform.model.payment;

import com.consultingplatform.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * UC6 – Manage Payment Methods
 *
 * Represents a saved payment method belonging to a client.
 * Referenced by Client.paymentMethods in the class diagram.
 *
 * Sensitive data (full card number, CVV) is never stored –
 * only masked/safe values are persisted (last4Digits, expiryDate, etc.).
 */
@Entity
@Table(name = "payment_methods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    // --- Credit / Debit card (stored masked) ---
    private String last4Digits;       // last 4 of card number
    private String expiryDate;        // MM/YY
    private String cardholderName;

    // --- PayPal ---
    private String paypalEmail;

    // --- Bank transfer (stored masked) ---
    private String last4AccountDigits; // last 4 of account number
    private String routingNumber;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isDefault == null) isDefault = false;
    }
}
