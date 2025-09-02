package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserResponseDTO getCurrentUserDetails(UserDetails userDetails);

    UserResponseDTO getUserDetailsByUsername(String username,UserDetails currentUserDetails);

    UserResponseDTO updateUserProfile(UserDetails currentUserDetails, @Valid UserProfileUpdateDTO updateDTO, MultipartFile profileImage);

    PagedResponseDTO<UserResponseDTO> getSuggestedUsers(UserDetails userDetails, Pageable pageable);

    PagedResponseDTO<UserResponseDTO> getAllUsers(UserDetails userDetails, Pageable pageable);

    void updateEmailNotificationSetting(UserDetails currentUserDetails, boolean enabled);
}
