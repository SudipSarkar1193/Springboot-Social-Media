package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.AuthResponseDTO;
import com.SSarkar.Xplore.dto.LoginRequestDTO;
import com.SSarkar.Xplore.dto.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.dto.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.UserProfile;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.security.jwt.JwtUtils;
import com.SSarkar.Xplore.service.contract.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // New
    private final JwtUtils jwtUtils; // New

    @Transactional
    @Override
    public UserResponseDTO registerUser(UserRegistrationRequestDTO registrationRequest) {
        log.debug("Attempting to register user with username: {} and email: {}",
                registrationRequest.getUsername(), registrationRequest.getEmail());

        // 1. Check if username or email already exists
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            log.warn("Registration failed: Username '{}' is already taken", registrationRequest.getUsername());
            throw new IllegalStateException("Username is already taken");
        }
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            log.warn("Registration failed: Email '{}' is already in use", registrationRequest.getEmail());
            throw new IllegalStateException("Email is already in use");
        }

        // 2. Create a new User entity
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());

        if (registrationRequest.isProfilePictureUrlValid()) {
            UserProfile userProfile = new UserProfile();
            userProfile.setProfilePictureUrl(registrationRequest.getProfilePictureUrl());
            user.setUserProfile(userProfile);
            log.debug("Profile picture URL set: {}", registrationRequest.getProfilePictureUrl());
        }

        // 3. Encode the password before saving
        String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());
        user.setPassword(encodedPassword);
        log.debug("Password encoded successfully");

        // 4. Save the new user to the database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with UUID: {}", savedUser.getUuid());

        // 5. Map the saved user to a response DTO (excluding password)
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setUuid(savedUser.getUuid());
        userResponse.setUsername(savedUser.getUsername());
        userResponse.setEmail(savedUser.getEmail());

        if (savedUser.getUserProfile() != null) {
            userResponse.setProfilePictureUrl(savedUser.getUserProfile().getProfilePictureUrl());
        } else {
            userResponse.setProfilePictureUrl(null);
        }

        return userResponse;
    }

    @Override
    public AuthResponseDTO loginUser(LoginRequestDTO loginRequest) {

        log.debug("Attempting login for user: {}", loginRequest.getUsername());

        // 1. Create Authentication object using UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        // 2. Authenticate the user using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        log.debug("Authentication successful for user: {}", loginRequest.getUsername());

        // 3. Set the authentication object in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Security context updated for user: {}", loginRequest.getUsername());

        // 3. Generate the JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateTokenFromUsername(userDetails);
        log.info("JWT generated for user: {}", userDetails.getUsername());

        // 5. Return the JWT in the response
        return new AuthResponseDTO(jwt);
    }
}
