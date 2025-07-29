package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserResponseDTO getCurrentUserDetails(UserDetails userDetails);

    UserResponseDTO getUserDetailsByUsername(String username);

    UserResponseDTO updateUserProfile(UserDetails currentUserDetails, @Valid UserProfileUpdateDTO updateDTO);
}
