package com.consultingplatform.booking.state;

import com.consultingplatform.booking.domain.Booking;
import java.time.OffsetDateTime;

/**
 * State after consultant accepts the booking
 */
public class ConfirmedState implements BookingState {
    
    @Override
    public void accept(Booking booking) {
        throw new IllegalStateException("Booking is already confirmed");
    }
    
    @Override
    public void reject(Booking booking) {
        throw new IllegalStateException("Cannot reject an already confirmed booking");
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
        booking.setStatus("CANCELLED");
        booking.setCancelledAt(OffsetDateTime.now());
        booking.setState(new CancelledState());
    }
    
    @Override
    public void processPayment(Booking booking) {
        // Payment processed -> move to PAID state
        booking.setStatus("PAID");
        booking.setState(new PaidState());
    }
    
    @Override
    public String getStateName() {
        return "CONFIRMED";
    }
}
