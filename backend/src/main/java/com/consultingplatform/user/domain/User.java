package com.consultingplatform.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Data
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "account_status", nullable = false)
    private String accountStatus = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Read-only view of the discriminator column. Stored in DB by JPA because
     * the inheritance strategy uses a discriminator column named "role".
     * Marked non-insertable/non-updatable so JPA manages it via the subclass
     * annotations (@DiscriminatorValue).
     */
    @Column(name = "role", insertable = false, updatable = false)
    private String role;

    /**
     * Computed full name from first and last name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Returns the `Role` enum corresponding to the discriminator value, or null
     * if the role column is not set or does not match an enum value.
     */
    public Role getRole() {
        if (this.role == null) return null;
        try {
            return Role.valueOf(this.role);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // Abstract methods from class diagram
    public abstract boolean login();
    
    public abstract void logout();
}
