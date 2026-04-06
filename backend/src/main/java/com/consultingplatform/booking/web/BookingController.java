package com.consultingplatform.booking.web;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.service.BookingService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import com.consultingplatform.security.CustomUserDetails;

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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) principal).getId();
            request.setClientId(userId);
        }
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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) principal).getId();
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !userId.equals(clientId)) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Not authorized");
            }
        }
        return bookingService.getClientBookings(clientId);
    }
}
