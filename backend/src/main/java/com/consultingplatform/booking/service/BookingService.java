package com.consultingplatform.booking.service;

import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.booking.web.BookingRequest;
import java.util.List;

public interface BookingService {

    Booking requestBooking(BookingRequest request);

    Booking getBookingById(Long bookingId);

    Booking cancelBooking(Long bookingId);

    List<Booking> getClientBookings(Long clientId);

}