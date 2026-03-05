package com.consultingplatform.booking.web;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.service.BookingService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking requestBooking(@RequestBody Booking booking) {
        return bookingService.requestBooking(booking);
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @PutMapping("/{id}/accept")
    public Booking acceptBooking(@PathVariable Long id) {
        return bookingService.acceptBooking(id);
    }

    @PutMapping("/{id}/reject")
    public Booking rejectBooking(@PathVariable Long id) {
        return bookingService.rejectBooking(id);
    }

    @PutMapping("/{id}/complete")
    public Booking completeBooking(@PathVariable Long id) {
        return bookingService.completeBooking(id);
    }

    @PutMapping("/{id}/payment")
    public Booking processPayment(@PathVariable Long id) {
        return bookingService.processPayment(id);
    }

    @GetMapping("/client/{clientId}")
    public List<Booking> getClientBookings(@PathVariable Long clientId) {
        return bookingService.getClientBookings(clientId);
    }
}
