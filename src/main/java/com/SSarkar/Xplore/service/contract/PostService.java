package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.post.*;
import com.SSarkar.Xplore.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface PostService {
    void createPost(CreatePostRequestDTO createPostRequest, List<MultipartFile> images, MultipartFile video, UserDetails currentUser);

    PagedResponseDTO<PostResponseDTO> getAllTopLevelPosts(Pageable pageable, UserDetails currentUser);

    PagedResponseDTO<PostResponseDTO> getAllFollowingPost(Pageable pageable, UserDetails currentUserDetails);

    PagedResponseDTO<PostResponseDTO> getFeedPosts(Pageable pageable, UserDetails currentUserDetails);

    PostResponseDTO getPostByUuid(UUID uuid, UserDetails currentUser);

    void deletePost(UUID uuid, UserDetails currentUser);

    void updatePost(UserDetails currentUser , UUID postUuid, PostUpdateDTO postUpdateDTO);

    public PostResponseDTO addCommentToPost(UUID parentPostUuid, CommentRequestDTO commentRequest, List<MultipartFile> images, UserDetails currentUserDetails);

    PagedResponseDTO<PostResponseDTO> getPostsByUser(UUID userUuid, Pageable pageable, UserDetails currentUser);

    String likePost(UUID postUuid, UserDetails currentUser);

    void unlikePost(UUID postUuid, UserDetails currentUser);

    PagedResponseDTO<PostResponseDTO> getLikedPostsByUser(UUID userUuid, Pageable pageable);

    String increaseShareCount(UUID postUuid);

    PagedResponseDTO<PostResponseDTO> getAllShorts(Pageable pageable, UserDetails currentUser);

}