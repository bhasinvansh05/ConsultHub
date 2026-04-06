package com.consultingplatform.consultant.repository;

import com.consultingplatform.consultant.domain.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByConsultantId(Long consultantId);

    List<AvailabilitySlot> findByServiceId(Long serviceId);

    @Query("SELECT COUNT(s) > 0 FROM AvailabilitySlot s WHERE s.consultantId = :consultantId " +
           "AND s.startAt < :endAt AND s.endAt > :startAt")
    boolean existsOverlappingSlot(@Param("consultantId") Long consultantId,
                                  @Param("startAt") OffsetDateTime startAt,
                                  @Param("endAt") OffsetDateTime endAt);
}
