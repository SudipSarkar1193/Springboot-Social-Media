package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.CommentRequestDTO;
import com.SSarkar.Xplore.dto.CreatePostRequestDTO;
import com.SSarkar.Xplore.dto.PagedResponseDTO;
import com.SSarkar.Xplore.dto.PostResponseDTO;
import com.SSarkar.Xplore.entity.Post;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.exception.ResourceNotFoundException;
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
import java.util.stream.Collectors;

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
        return mapPostToResponseDTO(savedPost,0);
    }

    @Override
    @Transactional
    public PostResponseDTO addCommentToPost(UUID parentPostUuid, CommentRequestDTO commentRequest, UserDetails currentUserDetails) {
        // 1. Find the author of the comment
        User author = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Find the parent post that is being commented on
        Post parentPost = postRepository.findByUuid(parentPostUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + parentPostUuid));

        // 3. Create the new Post entity (which is our comment)
        Post comment = new Post();
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(author);


        // 4. Check for and set the image URLs from the DTO
        if (commentRequest.getImageUrls() != null && !commentRequest.getImageUrls().isEmpty()) {
            comment.setImageUrls(commentRequest.getImageUrls());
        }

        // 5. Use the helper method to establish the bidirectional link
        parentPost.addComment(comment);

        // 6. Save the parent post. Due to `cascade=ALL`, the new comment will be saved as well.
        postRepository.save(parentPost);
        log.info("New comment with UUID: {} added to post with UUID: {}", comment.getUuid(), parentPost.getUuid());

        // 7. Map and return the DTO for the newly created comment
        return mapPostToResponseDTO(comment, 0); // Recursion depth starts at 0
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<PostResponseDTO> getAllPosts(Pageable pageable) {
        // This query is efficient because of the @EntityGraph on the repository method.
        Page<Post> postPage = postRepository.findAll(pageable);
        log.debug("Fetched {} posts from page {}", postPage.getNumberOfElements(), pageable.getPageNumber());

        List<PostResponseDTO> postResponseDTOList = new ArrayList<>();

        for (Post post : postPage.getContent()) {
            // Instead of manually building the DTO, we use our helper method.
            // It correctly handles the mapping of the post and any nested comments.
            // We use a recursion depth of '0' for the main feed to just show the post and comment count, not the full comment thread.
            PostResponseDTO postResp = mapPostToResponseDTO(post, 1);
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
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + uuid)); // Replace with a proper exception
        return mapPostToResponseDTO(post,1);
    }

    @Override
    @Transactional
    public void deletePost(UUID uuid, UserDetails currentUserDetails) {
        Post postToDelete = postRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + uuid));

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
    private PostResponseDTO mapPostToResponseDTO(Post post, int recursionDepth) {
        if (post == null) return null;

        PostResponseDTO dto = new PostResponseDTO();
        dto.setPostUuid(post.getUuid());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setImageUrls(post.getImageUrls());
        dto.setAuthorUsername(post.getAuthor().getUsername());
        dto.setAuthorUuid(post.getAuthor().getUuid());

        if (post.getParentPost() != null) {
            dto.setParentPostUuid(post.getParentPost().getUuid());
        }

        List<Post> commentList = post.getComments();

        dto.setCommentCount(commentList != null ? commentList.size() : 0);

        if (recursionDepth > 0 && commentList != null && !commentList.isEmpty()) {
            dto.setComments(commentList.stream()
                    .map(comment -> mapPostToResponseDTO(comment, recursionDepth - 1))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}