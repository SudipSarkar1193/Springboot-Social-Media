package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.CreatePostRequestDTO;
import com.SSarkar.Xplore.dto.PagedResponseDTO;
import com.SSarkar.Xplore.dto.PostResponseDTO;
import com.SSarkar.Xplore.entity.Post;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.repository.PostRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostResponseDTO createPost(CreatePostRequestDTO createPostRequest, UserDetails currentUserDetails) {
        // 1. Find the author User entity
        User author = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found while creating post"));

        // 2. Create the new Post entity
        Post newPost = new Post();
        newPost.setContent(createPostRequest.getContent());

        // Check if there are image URLs and set them
        if (createPostRequest.getImageUrls() != null) {
            newPost.setImageUrls(createPostRequest.getImageUrls());
        }

        // 3. Use the helper method on the parent (User) to establish the link
        author.addPost(newPost); // This syncs both sides of the relationship!

        // 4. Save the new Post
        // We save the 'child' side. Cascade settings will handle the rest.
        Post savedPost = postRepository.save(newPost);
        log.info("New post created with UUID: {} by user: {}", savedPost.getUuid(), author.getUsername());

        // 5. Map and return the DTO
        return mapPostToResponseDTO(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<PostResponseDTO> getAllPosts(Pageable pageable) {
        // This is the most important part: a single, efficient query!
        Page<Post> postPage = postRepository.findAll(pageable);
        log.debug("Fetched {} posts from page {}", postPage.getNumberOfElements(), pageable.getPageNumber());

        List<PostResponseDTO> postResponseDTOList = new ArrayList<>();

        // The for-loop is 100% acceptable.
        for (Post post : postPage.getContent()) {
            // Because of @EntityGraph, these calls do NOT trigger extra queries.
            String authorUsername = post.getAuthor().getUsername();
            UUID authorUuid = post.getAuthor().getUuid();

            PostResponseDTO postResp = new PostResponseDTO(
                    post.getUuid(),
                    post.getContent(),
                    post.getImageUrls(),
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    authorUsername,
                    authorUuid
            );
            postResponseDTOList.add(postResp);
        }

        return new PagedResponseDTO<>(
                postResponseDTOList,
                postPage.getNumber(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDTO getPostByUuid(UUID uuid) {
        Post post = postRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Post not found with UUID: " + uuid)); // Replace with a proper exception
        return mapPostToResponseDTO(post);
    }

    @Override
    @Transactional
    public void deletePost(UUID uuid, UserDetails currentUserDetails) {
        Post postToDelete = postRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Post not found with UUID: " + uuid));

        // Security Check: Ensure the person deleting the post is the author
        String currentUsername = currentUserDetails.getUsername();
        String authorUsername = postToDelete.getAuthor().getUsername();

        if (!currentUsername.equals(authorUsername)) {
            log.warn("ACCESS DENIED: User '{}' attempted to delete post '{}' owned by '{}'",
                    currentUsername, uuid, authorUsername);
            throw new AccessDeniedException("You are not authorized to delete this post");
        }

        postRepository.delete(postToDelete);
        log.info("Post with UUID: {} deleted successfully by user: {}", uuid, currentUsername);
    }


    // --- Helper Method ---
    private PostResponseDTO mapPostToResponseDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setPostUuid(post.getUuid());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setImageUrls(post.getImageUrls());


        // Note: We are accessing the author here, which is lazy-loaded.
        // This is safe because we are inside a @Transactional method.
        // Hibernate will automatically fetch the author when we access it.

        dto.setAuthorUsername(post.getAuthor().getUsername());
        dto.setAuthorUuid(post.getAuthor().getUuid());
        return dto;
    }
}