package com.consultingplatform.booking.state;

import com.consultingplatform.booking.domain.Booking;

/**
 * Terminal state after booking is cancelled
 */
public class CancelledState implements BookingState {
    
    @Override
    public void accept(Booking booking) {
        throw new IllegalStateException("Cannot accept a cancelled booking");
    }
    
    @Override
    public void reject(Booking booking) {
        throw new IllegalStateException("Booking is already cancelled");
    }
    
    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Cannot complete a cancelled booking");
    }
    
    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Booking is already cancelled");
    }
    
    @Override
    public void processPayment(Booking booking) {
        throw new IllegalStateException("Cannot process payment for cancelled booking");
    }
    
    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}
