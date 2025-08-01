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

        UserProfile userProfile = new UserProfile();


        if (registrationRequest.isProfilePictureUrlValid()) {

            String url = null;

            try {
                url = cloudinaryService.upload(registrationRequest.getProfilePictureUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            userProfile.setProfilePictureUrl(url);

            log.debug("Profile picture URL set: {}", registrationRequest.getProfilePictureUrl());
        }else{
            //https://res.cloudinary.com/dvsutdpx2/image/upload/v1732181213/ryi6ouf4e0mwcgz1tcxx.png
            userProfile.setProfilePictureUrl("https://res.cloudinary.com/dvsutdpx2/image/upload/v1732181213/ryi6ouf4e0mwcgz1tcxx.png");
            log.debug("Profile picture URL set with default url");
        }

        if(registrationRequest.getFullName() != null && !registrationRequest.getFullName().isEmpty()) {
            userProfile.setFullName(registrationRequest.getFullName());
            log.debug("Full name set: {}", registrationRequest.getFullName());

        }


        user.setUserProfile(userProfile);

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
