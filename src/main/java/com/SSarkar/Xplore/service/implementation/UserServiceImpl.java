package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    @Override
    public UserResponseDTO getCurrentUserDetails(UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Map the User entity to your UserResponseDTO
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setUuid(user.getUuid());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());

        if (user.getUserProfile() != null) {
            userResponse.setProfilePictureUrl(user.getUserProfile().getProfilePictureUrl());
        }

        return userResponse;
    }
}
