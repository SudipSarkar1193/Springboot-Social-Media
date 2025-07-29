package com.SSarkar.Xplore.service.contract;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface FollowService {
    void followUser(UUID userToFollowUuid, UserDetails currentUserDetails);
    void unfollowUser(UUID userToUnfollowUuid, UserDetails currentUserDetails);
}