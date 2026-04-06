package com.consultingplatform.consultant.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consultant_availability_slots")
@Data
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultant_id", nullable = false)
    private Long consultantId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
