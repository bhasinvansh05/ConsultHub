package com.consultingplatform.consultant.web;

import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.consultant.service.ConsultantService;
import com.consultingplatform.consultant.web.dto.*;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultant")
public class ConsultantController {

    private final ConsultantService consultantService;
    private final ConsultantRegistrationRepository consultantRegistrationRepository;

    public ConsultantController(ConsultantService consultantService,
                                ConsultantRegistrationRepository consultantRegistrationRepository) {
        this.consultantService = consultantService;
        this.consultantRegistrationRepository = consultantRegistrationRepository;
    }

    @GetMapping("/{consultantId}/registration-status")
    public ResponseEntity<Map<String, String>> getRegistrationStatus(@PathVariable Long consultantId) {
        return consultantRegistrationRepository.findByConsultantId(consultantId)
            .map(r -> ResponseEntity.ok(Map.of("status", r.getStatus().name())))
            .orElse(ResponseEntity.ok(Map.of("status", "NOT_FOUND")));
    }

    @PostMapping("/{consultantId}/availability")
    public ResponseEntity<AvailabilitySlotResponse> addAvailabilitySlot(
            @PathVariable Long consultantId,
            @Valid @RequestBody CreateAvailabilitySlotRequest request) {
        AvailabilitySlotResponse response = consultantService.addAvailabilitySlot(consultantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{consultantId}/availability")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailabilitySlots(
            @PathVariable Long consultantId) {
        return ResponseEntity.ok(consultantService.getAvailabilitySlots(consultantId));
    }

    @DeleteMapping("/{consultantId}/availability/{slotId}")
    public ResponseEntity<Void> deleteAvailabilitySlot(
            @PathVariable Long consultantId,
            @PathVariable Long slotId) {
        consultantService.deleteAvailabilitySlot(consultantId, slotId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{consultantId}/bookings")
    public ResponseEntity<List<ConsultantBookingResponse>> getBookings(
            @PathVariable Long consultantId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(consultantService.getBookingsByStatus(consultantId, status));
    }

    @PutMapping("/{consultantId}/bookings/{bookingId}/accept")
    public ResponseEntity<ConsultantBookingResponse> acceptBooking(
            @PathVariable Long consultantId,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(consultantService.acceptBooking(consultantId, bookingId));
    }

    @PutMapping("/{consultantId}/bookings/{bookingId}/reject")
    public ResponseEntity<ConsultantBookingResponse> rejectBooking(
            @PathVariable Long consultantId,
            @PathVariable Long bookingId,
            @RequestBody(required = false) BookingDecisionRequest request) {
        return ResponseEntity.ok(consultantService.rejectBooking(consultantId, bookingId, request));
    }

    @PutMapping("/{consultantId}/bookings/{bookingId}/complete")
    public ResponseEntity<ConsultantBookingResponse> completeBooking(
            @PathVariable Long consultantId,
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(consultantService.completeBooking(consultantId, bookingId));
    }
}
