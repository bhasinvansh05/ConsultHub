package com.consultingplatform.booking.service;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.repository.BookingRepository;
import com.consultingplatform.booking.web.BookingRequest;
import com.consultingplatform.consultant.domain.AvailabilitySlot;
import com.consultingplatform.consultant.repository.AvailabilitySlotRepository;
import com.consultingplatform.notification.service.NotificationService;

import com.consultingplatform.admin.domain.RefundPolicyConfig;
import com.consultingplatform.admin.service.SystemPolicyService;
import com.consultingplatform.payment.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import com.consultingplatform.security.CustomUserDetails;
import java.util.List;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final NotificationService notificationService;
    private final SystemPolicyService systemPolicyService;
    private final PaymentService paymentService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                            AvailabilitySlotRepository availabilitySlotRepository,
                            NotificationService notificationService,
                            SystemPolicyService systemPolicyService,
                            PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.notificationService = notificationService;
        this.systemPolicyService = systemPolicyService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT') and #request.clientId == principal.id")
    public Booking requestBooking(BookingRequest request) {
        // Fetch and validate the availability slot
        AvailabilitySlot slot = availabilitySlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Availability slot not found with ID: " + request.getSlotId()));

        // Validate slot is available
        if (!slot.getIsAvailable()) {
            throw new RuntimeException("This time slot is no longer available");
        }

        // Create booking - ALL details auto-populated from slot (no custom times allowed)
        Booking booking = new Booking();
        booking.setClientId(request.getClientId());
        booking.setConsultantId(slot.getConsultantId());  // From slot
        booking.setServiceId(slot.getServiceId());        // From slot
        booking.setAvailabilitySlotId(slot.getId());
        booking.setRequestedStartAt(slot.getStartAt());   // From slot
        booking.setRequestedEndAt(slot.getEndAt());       // From slot
        booking.setStatus("REQUESTED");

        // Mark slot as unavailable (prevent double booking)
        slot.setIsAvailable(false);
        availabilitySlotRepository.save(slot);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('CLIENT','CONSULTANT','ADMIN')")
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        // Enforce ownership: admins may cancel any booking; clients may cancel their own; consultants may cancel bookings assigned to them
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) principal).getId();
            var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isConsultant = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CONSULTANT"));
            if (!isAdmin) {
                boolean isClientOwner = userId.equals(booking.getClientId());
                boolean isConsultantOwner = isConsultant && userId.equals(booking.getConsultantId());
                if (!isClientOwner && !isConsultantOwner) {
                    throw new RuntimeException("Not authorized to cancel this booking");
                }
            }
        }

        boolean wasPaid = "PAID".equals(booking.getStatus());

        // Use State Pattern
        booking.cancel();

        // Calculate and process refund (percentage also used for client refund notification when wasPaid)
        double refundPercentage = 0.0;
        if (booking.getRequestedStartAt() != null) {
            long hoursUntilStart = ChronoUnit.HOURS.between(OffsetDateTime.now(), booking.getRequestedStartAt());
            RefundPolicyConfig policyConfig = systemPolicyService.getPolicyConfig("REFUND_POLICY", RefundPolicyConfig.class).orElse(null);

            if (policyConfig != null && policyConfig.getTiers() != null) {
                RefundPolicyConfig.RefundTier applicableTier = policyConfig.getTiers().stream()
                        .filter(tier -> hoursUntilStart >= tier.getHoursBefore())
                        .max(java.util.Comparator.comparingInt(RefundPolicyConfig.RefundTier::getHoursBefore))
                        .orElse(null);

                if (applicableTier != null) {
                    refundPercentage = applicableTier.getRefundPercentage();
                }
            } else {
                refundPercentage = 100.0;
            }

            if (refundPercentage > 0) {
                paymentService.processRefund(bookingId, refundPercentage);
            }
        }

        // Restore slot availability when booking is cancelled
        if (booking.getAvailabilitySlotId() != null) {
            availabilitySlotRepository.findById(booking.getAvailabilitySlotId())
                    .ifPresent(slot -> {
                        slot.setIsAvailable(true);
                        availabilitySlotRepository.save(slot);
                    });
        }

        Booking cancelled = bookingRepository.save(booking);
        notificationService.sendBookingCancelledNotifications(cancelled);
        if (wasPaid) {
            notificationService.sendPaidBookingCancelledRefundNotificationToClient(cancelled, refundPercentage);
        }
        return cancelled;
    }

    @Override
    @PreAuthorize("hasRole('CLIENT') and #clientId == principal.id")
    public List<Booking> getClientBookings(Long clientId) {

        return bookingRepository.findByClientId(clientId);

    }
}