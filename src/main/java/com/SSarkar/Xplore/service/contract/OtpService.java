package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.auth.UserRegistrationRequestDTO;

public interface OtpService {
    String generateAndStoreOtp(String email, UserRegistrationRequestDTO registrationRequest);

    String getOtp(String key);

    void clearOtp(String key);

    public UserRegistrationRequestDTO getRegistrationRequest(String email);
}
