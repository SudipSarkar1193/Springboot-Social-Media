package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.entity.Follow;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.exception.ResourceNotFoundException;
import com.SSarkar.Xplore.repository.FollowRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository ;

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
}
