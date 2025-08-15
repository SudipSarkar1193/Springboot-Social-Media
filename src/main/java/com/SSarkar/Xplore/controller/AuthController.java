package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.auth.*;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.service.contract.AuthService;
import com.SSarkar.Xplore.service.contract.EmailService;
import com.SSarkar.Xplore.service.contract.OtpService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Base path for all endpoints in this controller
@RequiredArgsConstructor
public class AuthController {


    private final OtpService otpService;
    private final EmailService emailService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {

        if (!isValidEmail(registrationRequest.getEmail())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email format");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String otp = otpService.generateAndStoreOtp(registrationRequest.getEmail(), registrationRequest);

        try {
            emailService.sendOtpEmail(registrationRequest.getEmail(), otp);
        } catch (MessagingException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error sending OTP");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "OTP sent to your email. Please verify to complete registration");
        return ResponseEntity.ok(successResponse);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Object> verifyOtp(@RequestBody VerifyOtpRequestDTO request) {
        String cachedOtp = otpService.getOtp(request.getEmail());

        if (cachedOtp != null && cachedOtp.equals(request.getOtp())) {
            UserRegistrationRequestDTO registrationRequest = otpService.getRegistrationRequest(request.getEmail());

            if (registrationRequest == null) {
                return ResponseEntity.badRequest().body("Registration data expired or not found");
            }

            otpService.clearOtp(request.getEmail());

            UserResponseDTO createdUser = authService.registerUser(registrationRequest);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody RegisterRequestDTO request) {
        // Get existing registration data
        UserRegistrationRequestDTO registrationRequest = otpService.getRegistrationRequest(request.getEmail());

        if (registrationRequest == null) {
            return ResponseEntity.badRequest().body("No registration data found. Please start registration again.");
        }

        // Generate new OTP while keeping existing registration data
        String newOtp = otpService.generateAndStoreOtp(request.getEmail(), registrationRequest);

        try {
            emailService.sendOtpEmail(request.getEmail(), newOtp);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending OTP");
        }

        return ResponseEntity.ok("A new OTP has been sent to your email");
    }


    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && email.matches(regex);
    }
}