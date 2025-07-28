package com.SSarkar.Xplore.service.contract;

import com.SSarkar.Xplore.dto.CreatePostRequestDTO;
import com.SSarkar.Xplore.dto.PagedResponseDTO;
import com.SSarkar.Xplore.dto.PostResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface PostService {
    PostResponseDTO createPost(CreatePostRequestDTO createPostRequest, UserDetails currentUser);
    PagedResponseDTO<PostResponseDTO> getAllPosts(Pageable pageable);
    PostResponseDTO getPostByUuid(UUID uuid);
    void deletePost(UUID uuid, UserDetails currentUser);
}