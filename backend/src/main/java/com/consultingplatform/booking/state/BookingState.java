package com.consultingplatform.booking.state;

import com.consultingplatform.booking.domain.Booking;

/**
 * State Pattern interface for Booking state transitions
 */
public interface BookingState {
    
    /**
     * Accept a booking (Consultant accepts)
     */
    void accept(Booking booking);
    
    /**
     * Reject a booking (Consultant rejects)
     */
    void reject(Booking booking);
    
    /**
     * Complete a booking (Mark as completed after service)
     */
    void complete(Booking booking);
    
    /**
     * Cancel a booking
     */
    void cancel(Booking booking);
    
    /**
     * Process payment for a booking
     */
    void processPayment(Booking booking);
    
    /**
     * Get the current state name
     */
    String getStateName();
}
