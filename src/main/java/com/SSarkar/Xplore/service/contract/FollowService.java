package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.dto.user.UserResponseDTO;
import com.SSarkar.Xplore.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.UUID;

public interface FollowService {
    String followUser(UUID userToFollowUuid, UserDetails currentUserDetails);
    void unfollowUser(User userToUnfollow, User currentUserDetails);
    PagedResponseDTO<UserResponseDTO> getFollowers(UUID userUuid, UserDetails currentUserDetails, Pageable pageable);
    PagedResponseDTO<UserResponseDTO> getFollowing(UUID userUuid, UserDetails currentUserdetails, Pageable pageable);
}