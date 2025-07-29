package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface FollowService {
    String followUser(UUID userToFollowUuid, UserDetails currentUserDetails);
    void unfollowUser(User userToUnfollow, User currentUserDetails);
}