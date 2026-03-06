package com.consultingplatform.booking.web;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.service.BookingService;

import jakarta.validation.Valid;
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
    public Booking requestBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.requestBooking(request);
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @GetMapping("/client/{clientId}")
    public List<Booking> getClientBookings(@PathVariable Long clientId) {
        return bookingService.getClientBookings(clientId);
    }
}
