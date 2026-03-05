package com.consultingplatform.booking.state;

import com.consultingplatform.booking.domain.Booking;
import java.time.OffsetDateTime;

/**
 * Initial state when a booking is requested by a client
 */
public class RequestedState implements BookingState {
    
    @Override
    public void accept(Booking booking) {
        // Consultant accepts the booking -> move to CONFIRMED state
        booking.setStatus("CONFIRMED");
        booking.setState(new ConfirmedState());
    }
    
    @Override
    public void reject(Booking booking) {
        // Consultant rejects the booking -> move to REJECTED state
        booking.setStatus("REJECTED");
        booking.setState(new RejectedState());
    }
    
    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Cannot complete a booking that hasn't been confirmed");
    }
    
    @Override
    public void cancel(Booking booking) {
        booking.setStatus("CANCELLED");
        booking.setCancelledAt(OffsetDateTime.now());
        booking.setState(new CancelledState());
    }
    
    @Override
    public void processPayment(Booking booking) {
        throw new IllegalStateException("Cannot process payment for unconfirmed booking");
    }
    
    @Override
    public String getStateName() {
        return "REQUESTED";
    }
}
