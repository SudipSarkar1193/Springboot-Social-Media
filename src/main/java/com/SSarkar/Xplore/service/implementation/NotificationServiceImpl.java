package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.notification.NotificationResponseDTO;
import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.entity.Notification;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.enums.NotificationType;
import com.SSarkar.Xplore.exception.ResourceNotFoundException;
import com.SSarkar.Xplore.repository.NotificationRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createNotification(User sender, User recipient, NotificationType type, UUID relatedEntityUuid) {
        // Skipping notifying users about their own actions
        if (sender.getId().equals(recipient.getId())) {
            log.debug("Skipping notification creation for user's own action. User: {}", sender.getUsername());
            return;
        }

        Notification notification = new Notification(recipient, sender, type, relatedEntityUuid);
        notificationRepository.save(notification);
        log.info("Saved notification of type {} for recipient {} from sender {}", type, recipient.getUsername(), sender.getUsername());

    }



    @Override
    @Transactional
    public PagedResponseDTO<NotificationResponseDTO> getNotificationsForUser(UserDetails currentUserDetails, Pageable pageable) {
        User recipient = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUserDetails.getUsername()));

        Page<Notification> notificationPage = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient, pageable);
        List<Notification> notificationsOnPage = notificationPage.getContent();

        // If no notifications are found, return an empty response
        List<NotificationResponseDTO> notificationDTOList = new ArrayList<>();

        // Map each Notification entity to NotificationResponseDTO
        for (Notification notification : notificationsOnPage) {
            notificationDTOList.add(mapEntityToDto(notification));
        }

        // Collecting unread notification IDs
        // This is done to mark them as read later
        List<Long> unreadNotificationIds = new ArrayList<>();
        for (Notification notification : notificationsOnPage) {
            if (!notification.isRead()) {
                unreadNotificationIds.add(notification.getId());
            }
        }

        // Mark notifications as read if there are any unread ones
        if (!unreadNotificationIds.isEmpty()) {
            notificationRepository.markAsRead(recipient, unreadNotificationIds);
            log.info("Marked {} notifications as read for user {}", unreadNotificationIds.size(), recipient.getUsername());
        }

        return new PagedResponseDTO<>(
                notificationDTOList,
                notificationPage.getNumber(),
                notificationPage.getTotalPages(),
                notificationPage.getTotalElements(),
                notificationPage.isLast()
        );
    }

    private NotificationResponseDTO mapEntityToDto(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setNotificationUuid(notification.getUuid());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRelatedEntityUuid(notification.getRelatedEntityUuid());


        if (notification.getSender() != null) {
            dto.setSenderUuid(notification.getSender().getUuid());
            dto.setSenderUsername(notification.getSender().getUsername());
            if (notification.getSender().getUserProfile() != null) {
                dto.setSenderProfilePictureUrl(notification.getSender().getUserProfile().getProfilePictureUrl());
            }
        }

        // Generate a human-readable message
        dto.setMessage(generateMessage(notification));

        return dto;
    }

    private String generateMessage(Notification notification) {
        String senderUsername = (notification.getSender() != null) ? notification.getSender().getUsername() : "Someone";
        switch (notification.getType()) {
            case NEW_FOLLOWER:
                return senderUsername + " started following you.";
            case POST_LIKE:
                return senderUsername + " liked your post.";
            case POST_COMMENT:
                return senderUsername + " commented on your post.";
            default:
                return "You have a new notification.";
        }
    }
}
