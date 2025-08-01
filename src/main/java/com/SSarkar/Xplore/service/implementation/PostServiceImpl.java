package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.post.*;
import com.SSarkar.Xplore.entity.Like;
import com.SSarkar.Xplore.entity.Post;
import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.entity.enums.NotificationType;
import com.SSarkar.Xplore.exception.ResourceNotFoundException;
import com.SSarkar.Xplore.repository.LikeRepository;
import com.SSarkar.Xplore.repository.PostRepository;
import com.SSarkar.Xplore.repository.UserRepository;
import com.SSarkar.Xplore.service.contract.CloudinaryService;
import com.SSarkar.Xplore.service.contract.NotificationService;
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

import java.io.IOException;
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
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public PostResponseDTO createPost(CreatePostRequestDTO createPostRequest, UserDetails currentUserDetails) {
        User author = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found while creating post"));

        Post newPost = new Post();
        newPost.setContent(createPostRequest.getContent());
        if (createPostRequest.getImageUrls() != null && !createPostRequest.getImageUrls().isEmpty()) {
            List<String> imgUrls = new ArrayList<>();

            for(String base64ImgString : createPostRequest.getImageUrls()) {
                String imgUrl = null;
                try {
                    imgUrl = cloudinaryService.upload(base64ImgString);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (imgUrl != null) {
                    imgUrls.add(imgUrl);
                } else {
                    log.warn("Failed to upload image, skipping: {}", base64ImgString);
                }
            }

            if (!imgUrls.isEmpty()) {
                newPost.setImageUrls(imgUrls);
            } else {
                log.warn("No valid image URLs provided, post will be created without images.");
            }
        }
        author.addPost(newPost);
        Post savedPost = postRepository.save(newPost);
        log.info("New post created with UUID: {} by user: {}", savedPost.getUuid(), author.getUsername());

        return mapPostToResponseDTO(savedPost, author, 0);
    }

    @Override
    @Transactional
    public PostResponseDTO addCommentToPost(UUID parentPostUuid, CommentRequestDTO commentRequest, UserDetails currentUserDetails) {
        User author = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post parentPost = postRepository.findByUuid(parentPostUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + parentPostUuid));
        Post comment = new Post();
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(author);
        if (commentRequest.getImageUrls() != null && !commentRequest.getImageUrls().isEmpty()) {
            comment.setImageUrls(commentRequest.getImageUrls());
        }
        parentPost.addComment(comment);
        postRepository.save(parentPost);
        log.info("New comment with UUID: {} added to post with UUID: {}", comment.getUuid(), parentPost.getUuid());
        notificationService.createNotification(author, parentPost.getAuthor(), NotificationType.POST_COMMENT, parentPost.getUuid());

        return mapPostToResponseDTO(comment, author, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<PostResponseDTO> getAllTopLevelPosts(Pageable pageable, UserDetails currentUserDetails) {
        Page<Post> postPage = postRepository.findAllByParentPostIsNull(pageable);
        log.debug("Fetched {} top-level posts from page {}", postPage.getNumberOfElements(), pageable.getPageNumber());

        User currentUser = getCurrentUserOrNull(currentUserDetails);

        List<PostResponseDTO> postResponseDTOList = postPage.getContent().stream()
                .map(post -> mapPostToResponseDTO(post, currentUser, 1))
                .collect(Collectors.toList());

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
    public PagedResponseDTO<PostResponseDTO> getPostsByUser(UUID userUuid, Pageable pageable, UserDetails currentUserDetails) {
        // Find the user whose posts we want to see
        User postAuthor = (User) userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        Page<Post> postPage = postRepository.getPostsByAuthor(postAuthor.getUuid(), pageable);
        log.debug("Fetched {} posts for user {}", postPage.getNumberOfElements(), postAuthor.getUsername());

        // Find the user who is making the request
        User currentUser = getCurrentUserOrNull(currentUserDetails);

        List<PostResponseDTO> postResponseDTOList = postPage.getContent().stream()
                .map(post -> mapPostToResponseDTO(post, currentUser, 1))
                .collect(Collectors.toList());

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
    public PostResponseDTO getPostByUuid(UUID uuid, UserDetails currentUserDetails) {
        Post post = postRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + uuid));
        User currentUser = getCurrentUserOrNull(currentUserDetails);
        return mapPostToResponseDTO(post, currentUser, 1);
    }

    @Override
    @Transactional
    public void deletePost(UUID uuid, UserDetails currentUserDetails) {
        Post postToDelete = postRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + uuid));

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

    @Override
    public Post updatePost(UserDetails currentUser, UUID postUuid,PostUpdateDTO postUpdateDTO) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(()->new ResourceNotFoundException("Post not found"));

        if(post.getAuthor().getUuid().equals(postUpdateDTO.getAuthorUUid())){
            throw new AccessDeniedException("You are not authorized to update this post");
        }

        post.setContent(postUpdateDTO.getContent());
        post.setImageUrls(postUpdateDTO.getImageUrls());

        postRepository.save(post);

        return post ;

    }

    @Override
    @Transactional
    public String likePost(UUID postUuid, UserDetails currentUserDetails) {
        User user = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + postUuid));

        if (likeRepository.findByUserAndPost(user, post).isPresent()) {
            unlikePost(postUuid, currentUserDetails);
            log.info("User {} unliked post {}", user.getUsername(), postUuid);
            return "Unliked the post";
        }

        Like newLike = new Like(user, post);
        likeRepository.save(newLike);
        log.info("User {} liked post {}", user.getUsername(), postUuid);
        notificationService.createNotification(user, post.getAuthor(), NotificationType.POST_LIKE, post.getUuid());

        return "Liked the post";
    }

    @Override
    @Transactional
    public void unlikePost(UUID postUuid, UserDetails currentUserDetails) {
        User user = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + postUuid));

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found for this user and post"));

        likeRepository.delete(like);
        log.info("User {} unliked post {}", user.getUsername(), postUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<PostResponseDTO> getLikedPostsByUser(UUID userUuid, Pageable pageable) {
        User user = (User) userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + userUuid));

        Page<Like> likedPostsPage = likeRepository.findByUser(user, pageable);

        List<PostResponseDTO> postResponseDTOs = likedPostsPage.getContent().stream()
                .map(like -> mapPostToResponseDTO(like.getPost(), user, 1))
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                postResponseDTOs,
                likedPostsPage.getNumber(),
                likedPostsPage.getTotalPages(),
                likedPostsPage.getTotalElements(),
                likedPostsPage.isLast()
        );
    }

    // --- HELPER METHODS ---

    private User getCurrentUserOrNull(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    private PostResponseDTO mapPostToResponseDTO(Post post, User currentUser, int recursionDepth) {
        if (post == null) return null;

        PostResponseDTO dto = new PostResponseDTO();
        dto.setPostUuid(post.getUuid());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setImageUrls(post.getImageUrls());
        dto.setAuthorUsername(post.getAuthor().getUsername());
        dto.setAuthorUuid(post.getAuthor().getUuid());

        if(post.getAuthor().getUserProfile().getProfilePictureUrl() != null) {
            dto.setAuthorProfilePictureUrl(post.getAuthor().getUserProfile().getProfilePictureUrl());
        } else {
            dto.setAuthorProfilePictureUrl("https://res.cloudinary.com/dvsutdpx2/image/upload/v1732181213/ryi6ouf4e0mwcgz1tcxx.png");

        }

        boolean isLiked = false;
        if (currentUser != null) {
            isLiked = likeRepository.findByUserAndPost(currentUser, post).isPresent();
        }
        dto.setLikedByCurrentUser(isLiked);

        dto.setLikeCount(postRepository.countLikesByPost(post));

        if (post.getParentPost() != null) {
            dto.setParentPostUuid(post.getParentPost().getUuid());
        }

        List<Post> commentList = post.getComments();
        dto.setCommentCount(commentList != null ? commentList.size() : 0);

        if (recursionDepth > 0 && commentList != null && !commentList.isEmpty()) {
            List<PostResponseDTO> commentDTOs = new ArrayList<>();
            for (Post comment : commentList) {
                commentDTOs.add(mapPostToResponseDTO(comment, currentUser, recursionDepth - 1));
            }
            dto.setComments(commentDTOs);
        }

        return dto;
    }
}