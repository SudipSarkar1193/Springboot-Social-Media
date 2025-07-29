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
        try{
            return getUserDetailsByUsername(currentUser.getUsername());
        }catch(UsernameNotFoundException e){
            throw e;
        }
    }

    @Override
    public UserResponseDTO getUserDetailsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Map the User entity to our UserResponseDTO
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setUuid(user.getUuid());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setFollowersCount(user.getFollowers().size());
        userResponse.setFollowingCount(user.getFollowing().size());

        if (user.getUserProfile() != null) {
            userResponse.setProfilePictureUrl(user.getUserProfile().getProfilePictureUrl());
        }

        return userResponse;
    }
}
