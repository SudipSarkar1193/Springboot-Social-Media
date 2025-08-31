package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.notification.NotificationResponseDTO;
import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.enums.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface NotificationService {

    void createNotification(User sender, User recipient, NotificationType type, UUID relatedEntityUuid);

    void deleteNotifications(UserDetails user);

    PagedResponseDTO<NotificationResponseDTO> getNotificationsForUser(UserDetails currentUserDetails, Pageable pageable);

    long getUnreadNotificationCount(UserDetails currentUserDetails);

//    void unsubscribeUser(String token);
}