package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.follow.FollowerDTO;
import com.SSarkar.Xplore.entity.Follow;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.enums.NotificationType;
import com.SSarkar.Xplore.exception.ResourceNotFoundException;
import com.SSarkar.Xplore.repository.FollowRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.FollowService;
import com.SSarkar.Xplore.service.contract.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository ;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public String followUser(UUID userToFollowUuid, UserDetails currentUserDetails) {
        User follower = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(()->new ResourceNotFoundException("User not found with username: " + currentUserDetails.getUsername()));

        User followee = (User) userRepository.findByUuid(userToFollowUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userToFollowUuid));


        if (follower.getId().equals(followee.getId())) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        // Check if the follower is already following the followee
        if (followRepository.findByFollowerAndFollowee(follower, followee).isPresent()) {
            unfollowUser(follower, followee);
            log.info("User {} is already following {}, so unfollowing them now.",
                    follower.getUsername(), followee.getUsername());
            return "You have unfollowed " + followee.getUsername();
        }

        Follow follow = new Follow(follower, followee);
        followRepository.save(follow);

        log.info("User {} started following {}", follower.getUsername(), followee.getUsername());

        notificationService.createNotification(follower, followee, NotificationType.NEW_FOLLOWER, follower.getUuid());

        return "You are now following " + followee.getUsername();
    }


    @Override
    @Transactional
    public void unfollowUser(User follower, User followee) {

        if (follower.getId().equals(followee.getId())) {
            throw new IllegalArgumentException("You cannot unfollow yourself.");
        }

        Follow follow = followRepository.findByFollowerAndFollowee(follower, followee)
                .orElseThrow(() -> {
                    log.info("User {} is not following {}", follower.getUsername(), followee.getUsername());
                    return new IllegalStateException("You are not following this user.");
                });

        followRepository.delete(follow);

        log.info("User {} has unfollowed {}", follower.getUsername(), followee.getUsername());

    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowerDTO> getFollowers(UUID userUuid) {
        User user = (User)userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        // Create a new list to hold our DTOs
        List<FollowerDTO> followersList = new ArrayList<>();

        // Loop through each 'Follow' relationship in the user's followers list
        for (Follow follow : user.getFollowers()) {
            User followerUser = follow.getFollower();
            String profilePictureUrl = (followerUser.getUserProfile() != null) ?
                    followerUser.getUserProfile().getProfilePictureUrl() : null;

            // Create a new DTO and add it to our list
            followersList.add(new FollowerDTO(followerUser.getUuid(), followerUser.getUsername(), profilePictureUrl));
        }

        return followersList;
    }


    @Override
    public List<FollowerDTO> getFollowing(UUID userUuid) {
        User user = (User) userRepository.findByUuid(userUuid)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        // Create a new list to hold our DTOs
        List<FollowerDTO> followingList = new ArrayList<>();

        // Loop through each 'Follow' relationship in the user's following list
        for(Follow follow : user.getFollowing()){
            User followeeUser = follow.getFollowee();
            String profilePictureUrl = (followeeUser.getUserProfile() != null) ?
                    followeeUser.getUserProfile().getProfilePictureUrl() : null;

            // Create a new DTO and add it to our list
            followingList.add(
                    new FollowerDTO(followeeUser.getUuid(), followeeUser.getUsername(), profilePictureUrl)
            );
        }

        return  followingList;
    }
}
