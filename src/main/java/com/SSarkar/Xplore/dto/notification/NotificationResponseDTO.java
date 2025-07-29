package com.SSarkar.Xplore.dto.notification;

import com.SSarkar.Xplore.entity.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponseDTO {
    private UUID notificationUuid;
    private NotificationType type;
    private UUID senderUuid;
    private String senderUsername;
    private String senderProfilePictureUrl;
    private UUID relatedEntityUuid; // e.g., Post UUID for likes/comments, User UUID for follows
    private boolean isRead;
    private Instant createdAt;
    private String message; // A human-readable message generated on the backend
}