package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.service.contract.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        List<UserResponseDTO> users = userService.getAllUsers(
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
    public ResponseEntity<List<UserResponseDTO>> getSuggestedUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        List<UserResponseDTO> suggestedUsers = userService.getSuggestedUsers(userDetails, limit);
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