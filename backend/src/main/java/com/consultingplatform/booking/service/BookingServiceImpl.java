package com.consultingplatform.booking.service;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.repository.BookingRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking requestBooking(Booking booking) {

        booking.setStatus("REQUESTED");

        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus("CANCELLED");

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getClientBookings(Long clientId) {

        return bookingRepository.findByClientId(clientId);

    }
}