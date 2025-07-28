package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.AuthResponseDTO;
import com.SSarkar.Xplore.dto.LoginRequestDTO;
import com.SSarkar.Xplore.dto.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.dto.UserResponseDTO;
import com.SSarkar.Xplore.service.contract.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Base path for all endpoints in this controller
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {
        UserResponseDTO createdUser = authService.registerUser(registrationRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
}