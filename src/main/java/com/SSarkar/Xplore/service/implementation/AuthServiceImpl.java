package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.auth.AuthResponseDTO;
import com.SSarkar.Xplore.dto.auth.LoginRequestDTO;
import com.SSarkar.Xplore.dto.auth.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.UserProfile;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.security.jwt.JwtUtils;
import com.SSarkar.Xplore.service.contract.AuthService;
import com.SSarkar.Xplore.service.contract.CloudinaryService;
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

import java.io.IOException;
import java.util.Random;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private  final CloudinaryService cloudinaryService;

    @Transactional
    @Override
    public UserResponseDTO registerUser(UserRegistrationRequestDTO registrationRequest) {
        log.debug("Attempting to register user with firstname: {} and email: {}",
                registrationRequest.getFirstName(), registrationRequest.getEmail());

        // 1. Check if email already exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            log.warn("Registration failed: Email '{}' is already in use", registrationRequest.getEmail());
            throw new IllegalStateException("Email is already in use");
        }

        // 2. Generate a unique username from firstName & lastName
        String uniqueUsername = generateUniqueUsername(
                registrationRequest.getFirstName(),
                registrationRequest.getLastName()
        );

        // 3. Create a new User entity
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(registrationRequest.getEmail());

        // 4. User profile setup
        UserProfile userProfile = new UserProfile();

        if (registrationRequest.isProfilePictureUrlValid()) {
            try {
                String url = cloudinaryService.upload(registrationRequest.getProfilePictureUrl());
                userProfile.setProfilePictureUrl(url);
                log.debug("Profile picture URL set: {}", url);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading profile picture", e);
            }
        } else {
            userProfile.setProfilePictureUrl(
                    "https://res.cloudinary.com/dvsutdpx2/image/upload/v1732181213/ryi6ouf4e0mwcgz1tcxx.png"
            );
            log.debug("Profile picture URL set with default url");
        }

        // 5. Build full name from firstName + lastName
        String fullName = registrationRequest.getFirstName() + " " + registrationRequest.getLastName();
        userProfile.setFullName(fullName);
        log.debug("Full name set: {}", fullName);

        user.setUserProfile(userProfile);

        // 6. Encode password
        String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());
        user.setPassword(encodedPassword);
        log.debug("Password encoded successfully");

        // 7. Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with UUID: {}", savedUser.getUuid());

        // 8. Map saved user to response DTO
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setUuid(savedUser.getUuid());
        userResponse.setUsername(savedUser.getUsername());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setProfilePictureUrl(
                savedUser.getUserProfile() != null
                        ? savedUser.getUserProfile().getProfilePictureUrl()
                        : null
        );

        return userResponse;
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = (firstName + lastName)
                .toLowerCase()
                .replaceAll("\\s+", ""); // Remove spaces

        String uniqueUsername = baseUsername;

        // If username already exists, append a random number to make it unique
        Random random = new Random();
        while (userRepository.existsByUsername(uniqueUsername)) {
            int randomNumber = 100 + random.nextInt(9000); // random number between 100 and 9099
            uniqueUsername = baseUsername + randomNumber;
        }

        return uniqueUsername;
    }

    @Override
    public AuthResponseDTO loginUser(LoginRequestDTO loginRequest) {

        // CHANGE HERE 👇
        log.debug("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

        // 1. Create Authentication object using UsernamePasswordAuthenticationToken
        //    Spring Security will pass the first argument (the principal) to your UserDetailsServiceImpl.
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(), // CHANGE HERE 👇
                loginRequest.getPassword()
        );

        // 2. Authenticate the user using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // CHANGE HERE 👇
        log.debug("Authentication successful for user: {}", loginRequest.getUsernameOrEmail());

        // 3. Set the authentication object in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // Get the authenticated user
        log.debug("Security context updated for user: {}", userDetails.getUsername());

        // 4. Generate the JWT
        String jwt = jwtUtils.generateTokenFromUsername(userDetails);
        log.info("JWT generated for user: {}", userDetails.getUsername());

        // 5. Return the JWT in the response
        return new AuthResponseDTO(jwt);
    }
}
