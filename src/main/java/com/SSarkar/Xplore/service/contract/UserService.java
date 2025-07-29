package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserResponseDTO getCurrentUserDetails(UserDetails userDetails);

    UserResponseDTO getUserDetailsByUsername(String username);
}
