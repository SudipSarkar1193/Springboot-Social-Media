package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.dto.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.UserProfile;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponseDTO registerUser(UserRegistrationRequestDTO registrationRequest) {
        // 1. Check if username or email already exists
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new IllegalStateException("Username is already taken");
        }
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalStateException("Email is already in use");
        }

        // 2. Create a new User entity
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());

        if(registrationRequest.isProfilePictureUrlValid()){
            UserProfile userProfile = new UserProfile();
            userProfile.setProfilePictureUrl(registrationRequest.getProfilePictureUrl());
            user.setUserProfile(userProfile);
        }


        // 3. Encode the password before saving
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

        // 4. Save the new user to the database
        User savedUser = userRepository.save(user);

        // 5. Map the saved user to a response DTO (without the password)
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setUuid(savedUser.getUuid());
        userResponse.setUsername(savedUser.getUsername());
        userResponse.setEmail(savedUser.getEmail());

        // Set the profile picture URL if it exists
        if (savedUser.getUserProfile() != null) {
            userResponse.setProfilePictureUrl(savedUser.getUserProfile().getProfilePictureUrl());
        } else {
            userResponse.setProfilePictureUrl(null); // Handle case where profile picture is not set
        }

        return userResponse;
    }
}
