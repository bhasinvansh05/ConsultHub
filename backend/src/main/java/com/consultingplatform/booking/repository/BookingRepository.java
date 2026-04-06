package com.consultingplatform.booking.repository;

import com.consultingplatform.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByClientId(Long clientId);

    List<Booking> findByConsultantId(Long consultantId);

    List<Booking> findByConsultantIdAndStatus(Long consultantId, String status);

    long countByStatus(String status);

    long countByServiceId(Long serviceId);
}