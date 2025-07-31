package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
    UserResponseDTO getCurrentUserDetails(UserDetails userDetails);

    UserResponseDTO getUserDetailsByUsername(String username,UserDetails currentUserDetails);

    UserResponseDTO updateUserProfile(UserDetails currentUserDetails, @Valid UserProfileUpdateDTO updateDTO);

    List<UserResponseDTO> getSuggestedUsers(UserDetails userDetails, int limit);

    List<UserResponseDTO> getAllUsers(UserDetails userDetails, Pageable pageable);
}
