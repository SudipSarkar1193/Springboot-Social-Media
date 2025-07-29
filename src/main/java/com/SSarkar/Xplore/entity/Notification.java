package com.SSarkar.Xplore.entity;

import com.SSarkar.Xplore.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid = UUID.randomUUID();

    /**
     * The user who should receive this notification.
     * We use FetchType.LAZY because we usually don't need the full User object
     * when we just want to list notifications. We can get the username from the sender.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /**
     * The user who triggered the notification (e.g., the one who followed, liked, or commented).
     * Can be null for system-generated notifications in the FUTURE.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    /**
     * Using an Enum for the notification type.
     * @Enumerated(EnumType.STRING) stores the enum name ("POST_LIKE") in the DB,
     * which is more readable than the default ordinal (0, 1, 2).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /**
     * A flag to indicate if the user has seen this notification.
     */
    @Column(nullable = false)
    private boolean isRead = false;

    /**
     * The UUID of the related entity. For a like/comment, this would be the Post UUID.
     * For a follow, it could be the sender's User UUID.
     * This helps the frontend navigate to the correct content when a notification is clicked.
     */
    private UUID relatedEntityUuid;

    @CreationTimestamp
    private Instant createdAt;

    public Notification(User recipient, User sender, NotificationType type, UUID relatedEntityUuid) {
        this.recipient = recipient;
        this.sender = sender;
        this.type = type;
        this.relatedEntityUuid = relatedEntityUuid;
    }
}