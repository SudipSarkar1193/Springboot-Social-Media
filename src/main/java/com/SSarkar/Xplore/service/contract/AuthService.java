package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.dto.UserResponseDTO;

public interface AuthService {
    UserResponseDTO registerUser(UserRegistrationRequestDTO registrationRequest);
}
