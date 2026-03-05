package com.consultingplatform.booking.state;

import com.consultingplatform.booking.domain.Booking;

/**
 * Terminal state after booking is rejected by consultant
 */
public class RejectedState implements BookingState {
    
    @Override
    public void accept(Booking booking) {
        throw new IllegalStateException("Cannot accept a rejected booking");
    }
    
    @Override
    public void reject(Booking booking) {
        throw new IllegalStateException("Booking is already rejected");
    }
    
    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Cannot complete a rejected booking");
    }
    
    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Booking is already rejected");
    }
    
    @Override
    public void processPayment(Booking booking) {
        throw new IllegalStateException("Cannot process payment for rejected booking");
    }
    
    @Override
    public String getStateName() {
        return "REJECTED";
    }
}
