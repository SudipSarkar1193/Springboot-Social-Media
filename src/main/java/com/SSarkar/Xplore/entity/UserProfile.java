package com.SSarkar.Xplore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long id; // We will share the primary key with the User entity

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profilePictureUrl;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // This is the OWNING side of the relationship
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This tells JPA that the 'id' field is both PK and FK
    @JoinColumn(name = "id") // The column name in this table will be 'id'
    private User user;

    // --- equals() and hashCode() for JPA Entities ---
    // This is crucial for performance and correctness when working with collections.
    // It ensures that Hibernate can correctly identify and manage entity instances.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}