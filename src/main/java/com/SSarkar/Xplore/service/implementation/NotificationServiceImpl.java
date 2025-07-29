package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.notification.NotificationResponseDTO;
import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.entity.Notification;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.enums.NotificationType;
import com.SSarkar.Xplore.repository.NotificationRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    public PagedResponseDTO<NotificationResponseDTO> getNotificationsForUser(UserDetails currentUserDetails, Pageable pageable) {
        return null;
    }
}
