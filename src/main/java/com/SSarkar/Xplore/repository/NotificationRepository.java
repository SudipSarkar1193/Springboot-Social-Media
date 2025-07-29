package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.Notification;
import com.SSarkar.Xplore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds a paginated list of notifications for a specific recipient,
     * ordered by creation time in descending order.
     * @param recipient The user who received the notifications.
     * @param pageable Pagination information.
     * @return A page of notifications.
     */
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    /**
     * Marks a list of notifications as read for a specific user.
     * This prevents one user from marking another user's notifications as read.
     * @Modifying is crucial for any query that is not a SELECT.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.id IN :notificationIds")
    void markAsRead(@Param("recipient") User recipient, @Param("notificationIds") List<Long> notificationIds);
}