package com.SSarkar.Xplore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long id; // We will share the primary key with the User entity

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String bio;

    private String profilePictureUrl;

    private String fullName;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // This is the OWNING side of the relationship
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This tells JPA that the 'id' field is both PK and FK
    @JoinColumn(name = "id") // The column name in this table will be 'id'
    @ToString.Exclude
    /**
     Prevent infinite recursion in toString .
     This is crucial for bidirectional relationships to avoid infinite recursion in toString methods.
     The @ToString.Exclude annotation prevents the UserProfile's toString method from including the User object, which would otherwise lead to an infinite loop when printing the UserProfile......because UserProfile has a reference back to User.
     This is a common pattern in JPA to manage bidirectional relationships.
    */
    private User user;

    // --- equals() and hashCode() for JPA Entities ---
    // This is crucial for performance and correctness when working with collections.
    // It ensures that Hibernate can correctly identify and manage entity instances.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        // The ID is the definitive identity for a UserProfile. This is safe and correct.
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // A constant is the safest choice for entities without a business key.
        return 31;
    }
}