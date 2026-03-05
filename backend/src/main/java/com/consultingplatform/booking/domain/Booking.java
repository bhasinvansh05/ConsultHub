package com.consultingplatform.booking.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "consultant_id", nullable = false)
    private Long consultantId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "availability_slot_id")
    private Long availabilitySlotId;

    @Column(name = "requested_start_at", nullable = false)
    private OffsetDateTime requestedStartAt;

    @Column(name = "requested_end_at", nullable = false)
    private OffsetDateTime requestedEndAt;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private OffsetDateTime requestedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}