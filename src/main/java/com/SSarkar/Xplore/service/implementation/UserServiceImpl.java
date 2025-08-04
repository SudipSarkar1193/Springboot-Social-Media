package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.dto.user.UserProfileUpdateDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.UserProfile;
import com.SSarkar.Xplore.exception.ResourceNotFoundException;
import com.SSarkar.Xplore.repository.FollowRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.CloudinaryService;
import com.SSarkar.Xplore.service.contract.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final CloudinaryService cloudinaryService ;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUserDetails(UserDetails currentUser) {
        return getUserDetailsByUsername(currentUser.getUsername(), currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserDetailsByUsername(String username,UserDetails currentUserDetails) {
        User user = userRepository.findByUsername(username).
                orElseThrow(()->new ResourceNotFoundException("User not found"));

        return mapUserToResponse(user,isFollowing(user,currentUserDetails)) ;
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
            String url = null;
            try {
                url = cloudinaryService.upload(updateDTO.getProfileImageUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            userProfile.setProfilePictureUrl(url);
        }

        // 4. Save the User entity. Because of the CascadeType.ALL setting on the
        // userProfile relationship, changes to the UserProfile will be persisted automatically.
        userRepository.save(user);

        // 5. Return the updated user details by reusing our existing mapping logic.
        return getUserDetailsByUsername(user.getUsername(),currentUserDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getSuggestedUsers(UserDetails userDetails, int limit) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        Pageable pageable = PageRequest.of(0, limit);
        List<User> suggestedUsers = userRepository.findTopUsersNotFollowedBy(currentUser.getUuid(), pageable);

        List<UserResponseDTO> userResponseDTOList = new ArrayList<>(suggestedUsers.size());
        for( User user : suggestedUsers) {
            UserResponseDTO userResponse = mapUserToResponse(user,false);
            userResponseDTOList.add(userResponse) ;
        }

        return userResponseDTOList;
    }

    @Override
    public PagedResponseDTO<UserResponseDTO> getAllUsers(UserDetails userDetails, Pageable pageable) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        // Fetch all users except the current user
        Page<User> userPage = userRepository.findAllUsersExceptForCurrentUser(currentUser.getUuid(), pageable);

        List<UserResponseDTO> userResponseDTOList = userPage.getContent().stream()
                .map(user -> mapUserToResponse(user, isFollowing(user, userDetails)))
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                userResponseDTOList,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.isLast()
        );
    }

    UserResponseDTO mapUserToResponse(User user,boolean isFollowing ) {
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setUuid(user.getUuid());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setFollowersCount(userRepository.countFollowers(user));
        userResponse.setFollowingCount(userRepository.countFollowing(user));
        userResponse.setPostCount(userRepository.countPosts(user));
        userResponse.setCurrentUserFollowing(isFollowing);

        if (user.getUserProfile() != null) {
            userResponse.setProfilePictureUrl(user.getUserProfile().getProfilePictureUrl());
            userResponse.setBio(user.getUserProfile().getBio());
            userResponse.setFullName(user.getUserProfile().getFullName());
        }

        return userResponse;
    }

    private boolean isFollowing(User user , UserDetails currentUserDetails){
        if (currentUserDetails == null) {
            return false;
        }

        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername()).orElse(null);

        if (currentUser == null || currentUser.getId().equals(user.getId())) {
            return false;
        }

        return followRepository.findByFollowerAndFollowee(currentUser, user).isPresent();
    }
}