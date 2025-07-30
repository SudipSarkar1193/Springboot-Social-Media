package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.UserProfile;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUserDetails(UserDetails currentUser) {
        try{
            return getUserDetailsByUsername(currentUser.getUsername());
        }catch(UsernameNotFoundException e){
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserDetailsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Map the User entity to our UserResponseDTO
        UserResponseDTO userResponse = mapUserToResponse(user);

        return userResponse;
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserProfile(UserDetails currentUserDetails, UserProfileUpdateDTO updateDTO) {
        // 1. Fetch the User entity based on the authenticated user's username.
        User user = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUserDetails.getUsername()));

        // 2. Get the associated UserProfile. If it doesn't exist, create a new one.
        UserProfile userProfile = user.getUserProfile();
        if (userProfile == null) {
            userProfile = new UserProfile();
            // Use the helper method in the User entity to ensure the bidirectional relationship is set correctly.
            user.setUserProfile(userProfile);
        }

        // 3. Update the UserProfile entity with data from the DTO.
        // We only update fields if they are provided in the request to avoid nullifying existing data.
        if (updateDTO.getFullName() != null) {
            userProfile.setFullName(updateDTO.getFullName());
        }
        if (updateDTO.getBio() != null) {
            userProfile.setBio(updateDTO.getBio());
        }
        if (updateDTO.getProfileImageUrl() != null) {
            userProfile.setProfilePictureUrl(updateDTO.getProfileImageUrl());
        }

        // 4. Save the User entity. Because of the CascadeType.ALL setting on the
        // userProfile relationship, changes to the UserProfile will be persisted automatically.
        userRepository.save(user);

        // 5. Return the updated user details by reusing our existing mapping logic.
        return getUserDetailsByUsername(user.getUsername());
    }

    @Override
    public List<UserResponseDTO> getSuggestedUsers(UserDetails userDetails, int limit) {
        // Fetch the current user
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        // Fetch those users whome the current user is not following
        List<User> suggestedUsers = userRepository.findTopUsersNotFollowedBy(currentUser.getUuid(), limit);

        List<UserResponseDTO> userResponseDTOList = new ArrayList<>(suggestedUsers.size());
        for( User user : suggestedUsers) {
            UserResponseDTO userResponse = mapUserToResponse(user);
            userResponseDTOList.add(userResponse) ;
        }

        return userResponseDTOList;

    }

    UserResponseDTO mapUserToResponse(User user) {
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
