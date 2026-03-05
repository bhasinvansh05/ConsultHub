package com.consultingplatform.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    private String id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Transient
    private PaymentStrategy strategy;

    @Column(name = "strategy_type", nullable = false)
    private String strategyType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String failureReason;

    @PrePersist
    protected void onCreate() {
        if (id == null)        id        = UUID.randomUUID().toString();
        if (timestamp == null) timestamp = LocalDateTime.now();
        if (status == null)    status    = PaymentStatus.PENDING;
    }
}
