package com.consultingplatform.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("CONSULTANT")
@Data
@EqualsAndHashCode(callSuper = true)
public class Consultant extends User {

    // From class diagram: availability, services, rating, isApproved
    // availability and services are managed via relationships in separate tables
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "is_approved")
    private Boolean isApproved = false;
    
    // Methods from class diagram
    public void addTimeSlot() {
        // Implementation delegated to AvailabilityService (consultant module)
    }
    
    public void acceptBooking(Long bookingId) {
        // Implementation delegated to BookingService
    }
    
    public void rejectBooking(Long bookingId) {
        // Implementation delegated to BookingService
    }

    @Override
    public boolean login() {
        // Consultant must be approved and active to login
        return this.getAccountStatus() != null && 
               this.getAccountStatus().equals("ACTIVE") &&
               this.isApproved != null && 
               this.isApproved;
    }

    @Override
    public void logout() {
        // Logout logic - typically handled by security framework
    }
}
