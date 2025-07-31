package com.SSarkar.Xplore.service.contract;

//import com.SSarkar.Xplore.dto.follow.FollowerDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface FollowService {
    String followUser(UUID userToFollowUuid, UserDetails currentUserDetails);
    void unfollowUser(User userToUnfollow, User currentUserDetails);
    List<UserResponseDTO> getFollowers(UUID userUuid,UserDetails currentUserDetails);
    List<UserResponseDTO> getFollowing(UUID userUuid,UserDetails currentUserdetails);
}