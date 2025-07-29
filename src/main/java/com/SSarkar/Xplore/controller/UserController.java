package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.service.contract.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        UserResponseDTO user = userService.getUserDetailsByUsername(username); // You'll need to create this service method
        return ResponseEntity.ok(user);
    }


    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @AuthenticationPrincipal UserDetails currentUserDetails,
            @Valid @RequestBody UserProfileUpdateDTO updateDTO
    ) {
        UserResponseDTO updatedUser = userService.updateUserProfile(currentUserDetails, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }
}