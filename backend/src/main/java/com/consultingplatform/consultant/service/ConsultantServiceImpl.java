package com.consultingplatform.consultant.service;

import com.consultingplatform.admin.service.ResourceNotFoundException;
import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.repository.BookingRepository;
import com.consultingplatform.consultant.domain.AvailabilitySlot;
import com.consultingplatform.consultant.repository.AvailabilitySlotRepository;
import com.consultingplatform.consultant.web.dto.*;
import com.consultingplatform.consultingservice.domain.ConsultingService;
import com.consultingplatform.consultingservice.repository.ConsultingServiceRepository;
import com.consultingplatform.notification.service.NotificationService;

import jakarta.transaction.Transactional;

import com.consultingplatform.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import com.consultingplatform.security.CustomUserDetails;

@Service
public class ConsultantServiceImpl implements ConsultantService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BookingRepository bookingRepository;
    private final ConsultingServiceRepository consultingServiceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ConsultantServiceImpl(AvailabilitySlotRepository availabilitySlotRepository,
                                 BookingRepository bookingRepository,
                                 ConsultingServiceRepository consultingServiceRepository,
                                 UserRepository userRepository,
                                 NotificationService notificationService) {
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.bookingRepository = bookingRepository;
        this.consultingServiceRepository = consultingServiceRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CONSULTANT') and #consultantId == principal.id")
    public AvailabilitySlotResponse addAvailabilitySlot(Long consultantId, CreateAvailabilitySlotRequest request) {
        // 1. Fetch Consultant (just checking if they exist using User repo)
        boolean consultantExists = userRepository.existsById(consultantId);
        if (!consultantExists) {
            throw new ResourceNotFoundException("Consultant not found");
        }

        // 2. Fetch the ConsultingService to ensure it exists and is active
        ConsultingService consultingService = consultingServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Consulting service not found"));

        boolean serviceIsActive = Boolean.TRUE.equals(consultingService.getIsActive());
        if (!serviceIsActive) {
            throw new IllegalStateException("Consulting service is inactive");
        }

        if (consultingService.getDurationMinutes() == null || consultingService.getDurationMinutes() <= 0) {
            throw new IllegalStateException("Consulting service has invalid duration");
        }

        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalStateException("end_at must be after start_at");
        }

        Duration requestedDuration = Duration.between(request.getStartAt(), request.getEndAt());
        Duration serviceDuration = Duration.ofMinutes(consultingService.getDurationMinutes());
        boolean slotMatchesServiceDuration = requestedDuration.equals(serviceDuration);
        if (!slotMatchesServiceDuration) {
            throw new IllegalStateException("Availability slot duration must match service duration");
        }

        boolean overlapping = availabilitySlotRepository.existsOverlappingSlot(
                consultantId, request.getStartAt(), request.getEndAt());
        if (overlapping) {
            throw new IllegalStateException("Overlapping availability slot exists");
        }

        // 3. Create and save the AvailabilitySlot
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setConsultantId(consultantId);
        slot.setServiceId(request.getServiceId());
        slot.setStartAt(request.getStartAt());
        slot.setEndAt(request.getEndAt());
        slot.setIsAvailable(true);

        AvailabilitySlot saved = availabilitySlotRepository.save(slot);
        return toSlotResponse(saved);
    }

    @Override
    public List<AvailabilitySlotResponse> getAvailabilitySlots(Long consultantId) {
        return availabilitySlotRepository.findByConsultantId(consultantId)
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    @Override
    public List<AvailabilitySlotResponse> getAvailabilitySlotsByServiceId(Long serviceId) {
        return availabilitySlotRepository.findByServiceId(serviceId)
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    @Override
    public void deleteAvailabilitySlot(Long consultantId, Long slotId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found"));
        if (!slot.getConsultantId().equals(consultantId)) {
            throw new IllegalStateException("Slot does not belong to this consultant");
        }
        availabilitySlotRepository.delete(slot);
    }

    @Override
    @PreAuthorize("hasRole('CONSULTANT') and #consultantId == principal.id")
    public List<ConsultantBookingResponse> getBookingsByStatus(Long consultantId, String status) {
        List<Booking> bookings;
        if (status != null && !status.isBlank()) {
            bookings = bookingRepository.findByConsultantIdAndStatus(consultantId, status);
        } else {
            bookings = bookingRepository.findByConsultantId(consultantId);
        }
        return bookings.stream().map(this::toBookingResponse).toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CONSULTANT') and #consultantId == principal.id")
    public ConsultantBookingResponse acceptBooking(Long consultantId, Long bookingId) {
        Booking booking = getBookingForConsultant(consultantId, bookingId);
        booking.accept();
        booking.setConsultantDecidedAt(OffsetDateTime.now());
        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingAcceptedNotificationToClient(saved);
        return toBookingResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CONSULTANT') and #consultantId == principal.id")
    public ConsultantBookingResponse rejectBooking(Long consultantId, Long bookingId, BookingDecisionRequest request) {
        Booking booking = getBookingForConsultant(consultantId, bookingId);
        booking.reject();
        booking.setConsultantDecidedAt(OffsetDateTime.now());
        if (request != null && request.getReason() != null) {
            booking.setRejectionReason(request.getReason());
        }
        
        // Restore slot availability when booking is rejected
        if (booking.getAvailabilitySlotId() != null) {
            availabilitySlotRepository.findById(booking.getAvailabilitySlotId())
                    .ifPresent(slot -> {
                        slot.setIsAvailable(true);
                        availabilitySlotRepository.save(slot);
                    });
        }
        
        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingRejectedNotificationToClient(saved, saved.getRejectionReason());
        return toBookingResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('CONSULTANT') and #consultantId == principal.id")
    public ConsultantBookingResponse completeBooking(Long consultantId, Long bookingId) {
        Booking booking = getBookingForConsultant(consultantId, bookingId);
        // Ensure payment has been processed before completing the booking
        if (!"PAID".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Cannot complete booking before payment is processed");
        }
        booking.complete();
        Booking saved = bookingRepository.save(booking);
        return toBookingResponse(saved);
    }

    // Ensure consultant ownership when necessary
    private Booking getBookingForConsultant(Long consultantId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getConsultantId().equals(consultantId)) {
            throw new IllegalStateException("Booking does not belong to this consultant");
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) principal).getId();
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !userId.equals(consultantId)) {
                throw new RuntimeException("Not authorized for this consultant booking");
            }
        }
        return booking;
    }

    private AvailabilitySlotResponse toSlotResponse(AvailabilitySlot slot) {
        return new AvailabilitySlotResponse(
                slot.getId(), slot.getConsultantId(), slot.getServiceId(), slot.getStartAt(),
                slot.getEndAt(), slot.getIsAvailable(), slot.getCreatedAt());
    }

    private ConsultantBookingResponse toBookingResponse(Booking booking) {
        return new ConsultantBookingResponse(
                booking.getId(), booking.getClientId(), booking.getConsultantId(),
                booking.getServiceId(), booking.getRequestedStartAt(), booking.getRequestedEndAt(),
                booking.getStatus(), booking.getRequestedAt(),
                booking.getConsultantDecidedAt(), booking.getCompletedAt(),
                booking.getRejectionReason());
    }
}
