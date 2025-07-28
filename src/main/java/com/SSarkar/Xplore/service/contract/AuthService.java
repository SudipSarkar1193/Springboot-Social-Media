package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.auth.AuthResponseDTO;
import com.SSarkar.Xplore.dto.auth.LoginRequestDTO;
import com.SSarkar.Xplore.dto.auth.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.dto.auth.UserResponseDTO;

public interface AuthService {
    UserResponseDTO registerUser(UserRegistrationRequestDTO registrationRequest);
    AuthResponseDTO loginUser(LoginRequestDTO loginRequest);
}
