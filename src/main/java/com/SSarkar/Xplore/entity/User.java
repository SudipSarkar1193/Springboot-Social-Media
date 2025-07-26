package com.SSarkar.Xplore.entity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "uuid")
})
@EqualsAndHashCode(of = "uuid")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is our new, stable business key 🔑
    @Column(nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // This is the INVERSE side of the relationship
    // cascade = CascadeType.ALL: Operations (persist, merge, remove) on User will cascade to UserProfile.
    // orphanRemoval = true: If the UserProfile is removed from this relationship, it should be deleted from the database.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;


    // --- Helper method for bidirectional consistency ---
    // This is crucial for keeping both sides of the relationship in sync.
    public void setUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            if (this.userProfile != null) {
                this.userProfile.setUser(null);
            }
        } else {
            userProfile.setUser(this);
        }
        this.userProfile = userProfile;
    }

}
