package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.dto.UserResponseDTO;
import com.SSarkar.Xplore.service.contract.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Base path for all endpoints in this controller
@AllArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {
        UserResponseDTO createdUser = authService.registerUser(registrationRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}