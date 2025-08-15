package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.auth.*;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.service.contract.AuthService;
import com.SSarkar.Xplore.service.contract.EmailService;
import com.SSarkar.Xplore.service.contract.OtpService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final OtpService otpService;
    private final EmailService emailService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {

        if (!isValidEmail(registrationRequest.getEmail())) {
            return buildErrorResponse("Invalid email format", HttpStatus.BAD_REQUEST);
        }

        String otp = otpService.generateAndStoreOtp(registrationRequest.getEmail(), registrationRequest);

        try {
            emailService.sendOtpEmail(registrationRequest.getEmail(), otp);
        } catch (MessagingException e) {
            return buildErrorResponse("Error sending OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return buildSuccessResponse(
                "OTP sent to your email. Please verify to complete registration",
                HttpStatus.OK
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequestDTO request) {
        log.info("Received OTP verification request for email: {}", request.getEmail());

        String cachedOtp = otpService.getOtp(request.getEmail());
        log.debug("Cached OTP from service: {}", cachedOtp);

        if (cachedOtp != null && cachedOtp.equals(request.getOtp())) {
            log.info("OTP match successful for email: {}", request.getEmail());

            UserRegistrationRequestDTO registrationRequest = otpService.getRegistrationRequest(request.getEmail());
            if (registrationRequest == null) {
                log.warn("Registration data expired or not found for email: {}", request.getEmail());
                return buildErrorResponse("Registration data expired or not found", HttpStatus.BAD_REQUEST);
            }

            log.debug("Registration request data found for email: {}", request.getEmail());
            otpService.clearOtp(request.getEmail());
            log.info("Cleared OTP from cache for email: {}", request.getEmail());

            UserResponseDTO createdUser = authService.registerUser(registrationRequest);
            log.info("User registered successfully: {}", createdUser);

            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

        } else if (!Objects.equals(cachedOtp, request.getOtp())) {
            log.warn("Incorrect OTP entered for email: {} | Provided OTP: {}", request.getEmail(), request.getOtp());
            return buildErrorResponse("Incorrect OTP", HttpStatus.BAD_REQUEST);
        } else {
            log.warn("Invalid or expired OTP for email: {}", request.getEmail());
            return buildErrorResponse("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody VerifyOtpRequestDTO request) {
        UserRegistrationRequestDTO registrationRequest = otpService.getRegistrationRequest(request.getEmail());

        if (registrationRequest == null) {
            return buildErrorResponse("No registration data found. Please start registration again.", HttpStatus.BAD_REQUEST);
        }

        String newOtp = otpService.generateAndStoreOtp(request.getEmail(), registrationRequest);

        try {
            emailService.sendOtpEmail(request.getEmail(), newOtp);
        } catch (MessagingException e) {
            return buildErrorResponse("Error sending OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return buildSuccessResponse("A new OTP has been sent to your email", HttpStatus.OK);
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && email.matches(regex);
    }

    // ✅ Helper methods for consistent Map<String, String> responses
    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    private ResponseEntity<Map<String, String>> buildSuccessResponse(String message, HttpStatus status) {
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", message);
        return ResponseEntity.status(status).body(successResponse);
    }
}
