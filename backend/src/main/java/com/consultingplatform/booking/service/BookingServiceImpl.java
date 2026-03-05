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
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Use State Pattern
        booking.cancel();

        return bookingRepository.save(booking);
    }

    @Override
    public Booking acceptBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Use State Pattern
        booking.accept();

        return bookingRepository.save(booking);
    }

    @Override
    public Booking rejectBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Use State Pattern
        booking.reject();

        return bookingRepository.save(booking);
    }

    @Override
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Use State Pattern
        booking.complete();

        return bookingRepository.save(booking);
    }

    @Override
    public Booking processPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Use State Pattern
        booking.processPayment();

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getClientBookings(Long clientId) {

        return bookingRepository.findByClientId(clientId);

    }
}