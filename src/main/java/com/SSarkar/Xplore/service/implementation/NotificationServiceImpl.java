package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.notification.NotificationResponseDTO;
import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.entity.Notification;
import com.SSarkar.Xplore.entity.Post;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.enums.NotificationType;
import com.SSarkar.Xplore.exception.ResourceNotFoundException;
import com.SSarkar.Xplore.repository.NotificationRepository;
import com.SSarkar.Xplore.repository.PostRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.EmailService;
import com.SSarkar.Xplore.service.contract.NotificationService;
import jakarta.mail.MessagingException;
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


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PostRepository postRepository;



    @Override
    @Transactional
    public void createNotification(User sender, User recipient, NotificationType type, UUID relatedEntityUuid,String comment) {
        // Skipping notifying users about their own actions
        if (sender.getId().equals(recipient.getId())) {
            log.debug("Skipping notification creation for user's own action. User: {}", sender.getUsername());
            return;
        }

        Notification notification = new Notification(recipient, sender, type, relatedEntityUuid);
        notificationRepository.save(notification);
        log.info("Saved notification of type {} for recipient {} from sender {}", type, recipient.getUsername(), sender.getUsername());

        if (recipient.isEmailNotificationsEnabled()) {
            Post post = postRepository.findByUuid(relatedEntityUuid).orElse(null);
            String postUrl = null ;
            if(post != null){
                postUrl = "https://xplore-v7f1.vercel.app/post/" + post.getUuid().toString();
            }
            // Send email notification
            try {
                emailService.sendNotificationEmail(recipient.getEmail(), "New Notification from Xplore", generateMessage(notification), sender.getUsername(),sender.getUserProfile().getProfilePictureUrl(),comment == null ?(post == null ? null : post.getContent()):comment,postUrl);
            } catch (MessagingException e) {
                log.error("Failed to send notification email to {}", recipient.getEmail(), e);
            }
        }
    }

    @Override
    @Transactional
    public void deleteNotifications(UserDetails currentUserDetails) {
        User recipient = findUserByDetails(currentUserDetails);
        notificationRepository.deleteAllByRecipient(recipient);
        log.info("Deleted all notifications for user {}", recipient.getUsername());
    }


    @Override
    @Transactional
    public PagedResponseDTO<NotificationResponseDTO> getNotificationsForUser(UserDetails currentUserDetails, Pageable pageable) {
        User recipient = findUserByDetails(currentUserDetails);

        Page<Notification> notificationPage = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient, pageable);

        List<NotificationResponseDTO> notificationDTOs = new ArrayList<>();
        List<Long> unreadNotificationIds = new ArrayList<>();

        // OPTIMIZATION: Process notifications in a single loop (O(N) complexity).
        notificationPage.getContent().forEach(notification -> {

            // 1. Map the entity to its DTO.
            notificationDTOs.add(mapEntityToDto(notification));
            // 2. Collect IDs of unread notifications to mark them as read.
            if (!notification.isRead()) {
                unreadNotificationIds.add(notification.getId());
            }
        });

        // Perform a single bulk update query if there are unread notifications.
        if (!unreadNotificationIds.isEmpty()) {
            notificationRepository.markAsRead(recipient, unreadNotificationIds);
            log.info("Marked {} notifications as read for user {}", unreadNotificationIds.size(), recipient.getUsername());
        }

        return new PagedResponseDTO<>(
                notificationDTOs,
                notificationPage.getNumber(),
                notificationPage.getTotalPages(),
                notificationPage.getTotalElements(),
                notificationPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true) // This is a read-only operation, which can be a performance hint for the DB.
    public long getUnreadNotificationCount(UserDetails currentUserDetails) {
        User recipient = findUserByDetails(currentUserDetails);
        return notificationRepository.countByRecipientAndIsReadFalse(recipient);
    }


    // -- HELPER methos ---
    private User findUserByDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));
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
        dto.setMessage(generateMessage(notification));
        return dto;
    }

    private String generateMessage(Notification notification) {
        String senderUsername = (notification.getSender() != null) ? notification.getSender().getUsername() : "Someone";

        Post post = null ;
        if(notification.getRelatedEntityUuid() != null)
            post = postRepository.findByUuid(notification.getRelatedEntityUuid()).orElse(null);

        switch (notification.getType()) {
            case NEW_FOLLOWER:
                return senderUsername + " started following you.";
            case POST_LIKE:
                if(post !=null && post.getParentPost() != null)
                    return senderUsername + " liked your comment.";
                else
                    return senderUsername + " liked your post.";
            case POST_COMMENT:
                if(post !=null && post.getParentPost() != null)
                    return senderUsername + " replied to your comment.";
                else
                    return senderUsername + " commented on your post.";

            case POST_CREATED:
                return "Your post has been successfully published.";


            default:
                return "You have a new notification.";
        }
    }
}