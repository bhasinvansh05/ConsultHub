package com.consultingplatform.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("CONSULTANT")
@Data
@EqualsAndHashCode(callSuper = true)
public class Consultant extends User {

    @Column(name = "rating")
    private Double rating;
    
    // Business logic handled by Consultant module
    // Approval status tracked in consultant_registrations table (Admin module)

    @Override
    public boolean login() {
        // Basic account status check
        // Approval verification handled by authentication service via consultant_registrations
        return this.getAccountStatus() != null && 
               this.getAccountStatus().equals("ACTIVE");
    }

    @Override
    public void logout() {
        // Logout logic handled by security framework
    }
}
