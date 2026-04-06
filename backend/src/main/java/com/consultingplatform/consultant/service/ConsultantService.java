package com.consultingplatform.consultant.service;

import com.consultingplatform.consultant.web.dto.*;

import java.util.List;

public interface ConsultantService {

    AvailabilitySlotResponse addAvailabilitySlot(Long consultantId, CreateAvailabilitySlotRequest request);

    List<AvailabilitySlotResponse> getAvailabilitySlots(Long consultantId);

    List<AvailabilitySlotResponse> getAvailabilitySlotsByServiceId(Long serviceId);

    void deleteAvailabilitySlot(Long consultantId, Long slotId);

    List<ConsultantBookingResponse> getBookingsByStatus(Long consultantId, String status);

    ConsultantBookingResponse acceptBooking(Long consultantId, Long bookingId);

    ConsultantBookingResponse rejectBooking(Long consultantId, Long bookingId, BookingDecisionRequest request);

    ConsultantBookingResponse completeBooking(Long consultantId, Long bookingId);
}
