package com.consultingplatform.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("CLIENT")
@Data
@EqualsAndHashCode(callSuper = true)
public class Client extends User {

    // From class diagram: bookingHistory and paymentMethods
    // These are managed via relationships, not stored directly
    
    // Methods from class diagram
    public void createBooking() {
        // Implementation delegated to BookingService
    }
    
    public void cancelBooking(Long bookingId) {
        // Implementation delegated to BookingService
    }
    
    public void viewBookingHistory() {
        // Implementation delegated to BookingService
    }
    
    public void processPayment(String paymentDetails) {
        // Implementation delegated to PaymentService
    }
    
    public void addPaymentMethod(String paymentMethod) {
        // Implementation delegated to PaymentService
    }

    @Override
    public boolean login() {
        // Basic login check - could be enhanced with authentication
        return this.getAccountStatus() != null && 
               this.getAccountStatus().equals("ACTIVE");
    }

    @Override
    public void logout() {
        // Logout logic - typically handled by security framework
    }
}
