package com.consultingplatform.booking.state;

import com.consultingplatform.booking.domain.Booking;
import java.time.OffsetDateTime;

/**
 * State after payment is processed
 */
public class PaidState implements BookingState {
    
    @Override
    public void accept(Booking booking) {
        throw new IllegalStateException("Booking is already paid");
    }
    
    @Override
    public void reject(Booking booking) {
        throw new IllegalStateException("Cannot reject a paid booking");
    }
    
    @Override
    public void complete(Booking booking) {
        // Complete after service is done -> move to COMPLETED state
        booking.setStatus("COMPLETED");
        booking.setCompletedAt(OffsetDateTime.now());
        booking.setState(new CompletedState());
    }
    
    @Override
    public void cancel(Booking booking) {
        // Paid bookings can be cancelled (may trigger refund process)
        booking.setStatus("CANCELLED");
        booking.setCancelledAt(OffsetDateTime.now());
        booking.setState(new CancelledState());
    }
    
    @Override
    public void processPayment(Booking booking) {
        throw new IllegalStateException("Payment already processed");
    }
    
    @Override
    public String getStateName() {
        return "PAID";
    }
}
