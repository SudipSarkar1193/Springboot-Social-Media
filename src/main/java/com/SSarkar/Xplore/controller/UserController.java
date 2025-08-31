package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.dto.user.NotificationSettingUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.service.contract.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDTO userResponse = userService.getCurrentUserDetails(userDetails);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/me/notification-settings")
    public ResponseEntity<Map<String, String>> updateNotificationSettings(
            @AuthenticationPrincipal UserDetails currentUserDetails,
            @RequestBody NotificationSettingUpdateDTO settingUpdateDTO
    ) {
        log.debug("request to update notification settings: {}", settingUpdateDTO);
        userService.updateEmailNotificationSetting(currentUserDetails, settingUpdateDTO.isEnabled());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification settings updated successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponseDTO<UserResponseDTO>> getAllUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PagedResponseDTO<UserResponseDTO> users = userService.getAllUsers(
                userDetails,
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(users);
    }


    @GetMapping("/profile/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails currentUserDetails
    ) {
        UserResponseDTO user = userService.getUserDetailsByUsername(username,currentUserDetails);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<PagedResponseDTO<UserResponseDTO>> getSuggestedUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PagedResponseDTO<UserResponseDTO> suggestedUsers = userService.getSuggestedUsers(
                userDetails,
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(suggestedUsers);
    }


    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @AuthenticationPrincipal UserDetails currentUserDetails,
            @Valid @RequestBody UserProfileUpdateDTO updateDTO
    ) {
        UserResponseDTO updatedUser = userService.updateUserProfile(currentUserDetails, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }
}