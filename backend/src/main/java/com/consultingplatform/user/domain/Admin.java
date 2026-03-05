package com.consultingplatform.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("ADMIN")
@Data
@EqualsAndHashCode(callSuper = true)
public class Admin extends User {

    // From class diagram: permissions (stored as List<String>)
    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions; // JSON or comma-separated list
    
    // Methods from class diagram
    public void approveConsultant(Long consultantId) {
        // Implementation delegated to ConsultantApprovalService (admin module)
    }
    
    public void defineSystemPolicy(String policyName) {
        // Implementation delegated to SystemPolicyService (admin module)
    }

    @Override
    public boolean login() {
        // Admin must be active to login
        return this.getAccountStatus() != null && 
               this.getAccountStatus().equals("ACTIVE");
    }

    @Override
    public void logout() {
        // Logout logic - typically handled by security framework
    }
}
