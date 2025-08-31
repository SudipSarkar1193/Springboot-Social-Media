package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.notification.NotificationResponseDTO;
import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.service.contract.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications") // Using API versioning in the URL is a good practice.
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping
    public ResponseEntity<PagedResponseDTO<NotificationResponseDTO>> getNotificationsForCurrentUser(
            @AuthenticationPrincipal UserDetails currentUserDetails,
            @PageableDefault(size = 15) Pageable pageable) {

        PagedResponseDTO<NotificationResponseDTO> notifications = notificationService.getNotificationsForUser(currentUserDetails, pageable);
        return ResponseEntity.ok(notifications);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteAllNotifications(@AuthenticationPrincipal UserDetails currentUserDetails) {
        notificationService.deleteNotifications(currentUserDetails);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications have been cleared.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(
            @AuthenticationPrincipal UserDetails currentUserDetails) {

        long count = notificationService.getUnreadNotificationCount(currentUserDetails);
        Map<String, Long> response = Collections.singletonMap("count", count);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/unsubscribe")
//    public ResponseEntity<String> unsubscribe(@RequestParam("token") String token) {
//        notificationService.unsubscribeUser(token);
//        return ResponseEntity.ok("You have been successfully unsubscribed from email notifications.");
//    }
}