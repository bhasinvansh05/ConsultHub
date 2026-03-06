package com.consultingplatform.booking.service;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.repository.BookingRepository;
import com.consultingplatform.booking.web.BookingRequest;
import com.consultingplatform.consultant.domain.AvailabilitySlot;
import com.consultingplatform.consultant.repository.AvailabilitySlotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                            AvailabilitySlotRepository availabilitySlotRepository) {
        this.bookingRepository = bookingRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Override
    @Transactional
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
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Use State Pattern
        booking.cancel();

        // Restore slot availability when booking is cancelled
        if (booking.getAvailabilitySlotId() != null) {
            availabilitySlotRepository.findById(booking.getAvailabilitySlotId())
                    .ifPresent(slot -> {
                        slot.setIsAvailable(true);
                        availabilitySlotRepository.save(slot);
                    });
        }

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getClientBookings(Long clientId) {

        return bookingRepository.findByClientId(clientId);

    }
}