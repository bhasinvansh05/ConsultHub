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

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @GetMapping("/client/{clientId}")
    public List<Booking> getBookings(@PathVariable Long clientId) {
        return bookingService.getClientBookings(clientId);
    }
}
