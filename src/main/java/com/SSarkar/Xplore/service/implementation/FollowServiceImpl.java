package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository ;
    private final NotificationService notificationService;
    private final UserServiceImpl userServiceImpl;


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

        if (followRepository.findByFollowerAndFollowee(follower, followee).isPresent()) {
            unfollowUser(follower, followee);
            log.info("User {} is already following {}, so unfollowing them now.",
                    follower.getUsername(), followee.getUsername());
            return "You have unfollowed " + followee.getUsername();
        }

        Follow follow = new Follow(follower, followee);
        followRepository.save(follow);

        log.info("User {} started following {}", follower.getUsername(), followee.getUsername());

        notificationService.createNotification(follower, followee, NotificationType.NEW_FOLLOWER, follower.getUuid(),null);

        return "You are now following " + followee.getUsername();
    }


    @Override
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
    public PagedResponseDTO<UserResponseDTO> getFollowers(UUID userUuid, UserDetails currentUserDetails, Pageable pageable) {
        User user = (User) userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        Page<Follow> followerPage = followRepository.findByFollowee(user, pageable);
        List<User> followers = followerPage.getContent().stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());

        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername()).orElse(null);

        Set<UUID> followingUuids = (currentUser != null)
                ? followRepository.findFollowingUuidsByCurrentUserAndUsers(currentUser, followers)
                : Set.of();

        List<UserResponseDTO> followersList = followers.stream()
                .map(follower -> {
                    boolean isFollowing = followingUuids.contains(follower.getUuid());
                    return userServiceImpl.mapUserToResponse(follower, isFollowing);
                })
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                followersList,
                followerPage.getNumber(),
                followerPage.getTotalPages(),
                followerPage.getTotalElements(),
                followerPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<UserResponseDTO> getFollowing(UUID userUuid, UserDetails currentUserDetails, Pageable pageable) {
        User user = (User) userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        Page<Follow> followingPage = followRepository.findByFollower(user, pageable);
        List<User> following = followingPage.getContent().stream()
                .map(Follow::getFollowee)
                .collect(Collectors.toList());

        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername()).orElse(null);

        Set<UUID> followingUuids = (currentUser != null)
                ? followRepository.findFollowingUuidsByCurrentUserAndUsers(currentUser, following)
                : Set.of();

        List<UserResponseDTO> followingList = following.stream()
                .map(followee -> {
                    boolean isFollowing = followingUuids.contains(followee.getUuid());
                    return userServiceImpl.mapUserToResponse(followee, isFollowing);
                })
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                followingList,
                followingPage.getNumber(),
                followingPage.getTotalPages(),
                followingPage.getTotalElements(),
                followingPage.isLast()
        );
    }
}