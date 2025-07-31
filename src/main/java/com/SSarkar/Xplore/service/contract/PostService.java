package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.post.CommentRequestDTO;
import com.SSarkar.Xplore.dto.post.CreatePostRequestDTO;
import com.SSarkar.Xplore.dto.post.PagedResponseDTO;
import com.SSarkar.Xplore.dto.post.PostResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface PostService {
    PostResponseDTO createPost(CreatePostRequestDTO createPostRequest, UserDetails currentUser);

    PagedResponseDTO<PostResponseDTO> getAllTopLevelPosts(Pageable pageable, UserDetails currentUser);

    PostResponseDTO getPostByUuid(UUID uuid, UserDetails currentUser);

    void deletePost(UUID uuid, UserDetails currentUser);

    PostResponseDTO addCommentToPost(UUID parentPostUuid, CommentRequestDTO commentRequest, UserDetails currentUser);

    PagedResponseDTO<PostResponseDTO> getPostsByUser(UUID userUuid, Pageable pageable, UserDetails currentUser);

    String likePost(UUID postUuid, UserDetails currentUser);

    void unlikePost(UUID postUuid, UserDetails currentUser);

    PagedResponseDTO<PostResponseDTO> getLikedPostsByUser(UUID userUuid, Pageable pageable);
}