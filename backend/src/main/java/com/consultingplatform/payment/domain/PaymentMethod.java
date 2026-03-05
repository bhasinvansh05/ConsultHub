package com.consultingplatform.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    private String last4Digits;
    private String expiryDate;
    private String cardholderName;
    private String paypalEmail;
    private String last4AccountDigits;
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
