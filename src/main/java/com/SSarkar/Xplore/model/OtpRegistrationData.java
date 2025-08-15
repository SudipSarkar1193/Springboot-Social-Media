package com.SSarkar.Xplore.model;

import com.SSarkar.Xplore.dto.auth.UserRegistrationRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpRegistrationData {
    private String otp;
    private UserRegistrationRequestDTO registrationRequest;
}

