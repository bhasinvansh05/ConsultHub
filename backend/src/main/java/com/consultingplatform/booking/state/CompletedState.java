package com.consultingplatform.booking.state;

import com.consultingplatform.booking.domain.Booking;

/**
 * Terminal state after booking is completed
 */
public class CompletedState implements BookingState {
    
    @Override
    public void accept(Booking booking) {
        throw new IllegalStateException("Booking is already completed");
    }
    
    @Override
    public void reject(Booking booking) {
        throw new IllegalStateException("Cannot reject a completed booking");
    }
    
    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Booking is already completed");
    }
    
    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Cannot cancel a completed booking");
    }
    
    @Override
    public void processPayment(Booking booking) {
        throw new IllegalStateException("Cannot process payment for completed booking");
    }
    
    @Override
    public String getStateName() {
        return "COMPLETED";
    }
}
