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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
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

    /**
     * Helper method to compute the depth of posts for a single request.
     * This is thread-safe as it operates on local variables.
     */
    private void computeAndStoreDepths(List<Post> posts) {
        List<Post> postsToUpdate = new ArrayList<>();
        for (Post post : posts) {
            if (post.getDepth() == null) {
                int depth = 0;
                Post parent = post.getParentPost();
                while (parent != null) {
                    depth++;
                    parent = parent.getParentPost();
                }
                post.setDepth(depth);
                postsToUpdate.add(post);
            }
        }

        if (!postsToUpdate.isEmpty()) {
            postRepository.saveAll(postsToUpdate);
        }
    }
    @Override
    @Transactional
    @Async
    public void createPost(CreatePostRequestDTO createPostRequest, List<MultipartFile> images, MultipartFile video, UserDetails currentUserDetails) {

        System.out.println();
        System.out.println();
        log.info("Creating post for user: {}", currentUserDetails.getUsername());
        System.out.println("Creating post for user: " + currentUserDetails.getUsername());
        System.out.println();
        System.out.println();

        User author = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found while creating post"));

        Post newPost = new Post();
        newPost.setContent(createPostRequest.getContent());
        newPost.setDepth(0);

        try {
            if (video != null && !video.isEmpty()) {
                // This is a VIDEO_SHORT
                newPost.setPostType(Post.PostType.VIDEO_SHORT);

                System.out.println();System.out.println();
                System.out.println("---Uploading video to cloudinary---");
                System.out.println();
                String videoUrl = cloudinaryService.uploadVideo(video.getInputStream());

                System.out.println();
                System.out.println("---Uploaded video to cloudinary---");
                System.out.println();
                newPost.setVideoUrl(videoUrl);

            } else if (images != null && !images.isEmpty()) {
                // This is a TEXT_IMAGE (post with images)
                newPost.setPostType(Post.PostType.TEXT_IMAGE);
                List<String> imgUrls = new ArrayList<>();
                for (MultipartFile file : images) {
                    String imgUrl = cloudinaryService.upload(file.getInputStream());
                    if (imgUrl != null) {
                        imgUrls.add(imgUrl);
                    }
                }
                newPost.setImageUrls(imgUrls);

            } else {
                // This is a text-only post
                newPost.setPostType(Post.PostType.TEXT_IMAGE);
            }

            // Save the post and send notification ONLY after all uploads are successful
            author.addPost(newPost);
            Post savedPost = postRepository.save(newPost);
            System.out.println();
            System.out.println();
            log.info("New post created with UUID: {} by user: {}", savedPost.getUuid(), author.getUsername());
            System.out.println();
            System.out.println("Sending notification");
            System.out.println();
            notificationService.createNotification(author, author, NotificationType.POST_CREATED, savedPost.getUuid(), null);

        } catch (IOException e) {
            log.error("Error uploading media to Cloudinary!", e);
            // By throwing a runtime exception, the transaction will be rolled back
            // and the post will not be saved if the upload fails.
            throw new RuntimeException("Failed to upload media", e);
        }
    }

    @Override
    @Transactional
    public PostResponseDTO addCommentToPost(UUID parentPostUuid, CommentRequestDTO commentRequest, List<MultipartFile> images, UserDetails currentUserDetails) {
        User author = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post parentPost = postRepository.findByUuid(parentPostUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + parentPostUuid));

        Post comment = new Post();
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(author);

        // Calculate and set depth
        int depth = 0;
        Post parent = parentPost;
        while (parent != null) {
            depth++;
            parent = parent.getParentPost();
        }
        comment.setDepth(depth);

        // Logic to handle MultipartFile images
        if (images != null && !images.isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : images) {
                try {
                    String imgUrl = cloudinaryService.upload(file.getInputStream());
                    if (imgUrl != null) {
                        urls.add(imgUrl);
                    }
                } catch (IOException e) {
                    log.error("Failed to upload comment image", e);
                    throw new RuntimeException("Failed to upload comment image",e);
                }
            }
            comment.setImageUrls(urls);
        }

        // Save the comment first to make it a persistent entity
        Post savedComment = postRepository.save(comment);

        // Now add the persistent comment to the parent post
        parentPost.addComment(savedComment);
        postRepository.save(parentPost);

        //log.info("New comment with UUID: {} added to post with UUID: {}", savedComment.getUuid(), parentPost.getUuid());
        notificationService.createNotification(author, parentPost.getAuthor(), NotificationType.POST_COMMENT, parentPost.getUuid(), commentRequest.getContent());

        PostResponseDTO response = mapPostToResponseDTO(savedComment, author, 0, Collections.emptyMap(), Collections.emptySet());
        response.setDepth(depth); // Manually set calculated depth
        return response;
    }

    @Override
    @Transactional
    public PagedResponseDTO<PostResponseDTO> getAllTopLevelPosts(Pageable pageable, UserDetails currentUserDetails) {

        Page<Post> postPage = postRepository.findAllByParentPostIsNull(pageable);
        log.debug("Fetched {} top-level posts from page {}", postPage.getNumberOfElements(), pageable.getPageNumber());

        List<Post> posts = postPage.getContent();
        computeAndStoreDepths(posts);

        User currentUser = getCurrentUserOrNull(currentUserDetails);

        // Fetch like counts in bulk
        Map<UUID, Long> likeCounts = postRepository.countLikesForPosts(posts).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result.get("postUuid"),
                        result -> (Long) result.get("likeCount")));

        // Fetch liked statuses in bulk
        Set<UUID> likedPostUuids = (currentUser != null)
                ? likeRepository.findLikedPostUuidsByUserAndPosts(currentUser, posts)
                : Collections.emptySet();

        List<PostResponseDTO> postResponseDTOList = posts.stream()
                .map(post -> mapPostToResponseDTO(post, currentUser, 1, likeCounts, likedPostUuids))
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
    @Transactional
    public PagedResponseDTO<PostResponseDTO> getAllFollowingPost(Pageable pageable, UserDetails currentUserDetails) {

        User currentUser = getCurrentUserOrNull(currentUserDetails);

        log.debug("User found in getAllFollowingPost {} ",currentUser.getUsername());
        log.debug("inside getAllFollowingPost!") ;

        if (currentUser == null) {
            return new PagedResponseDTO<>(Collections.emptyList(), 0, 0, 0, true);
        }

        Page<Post> postPage = postRepository.findPostsByFollowing(currentUser.getUuid(), pageable);
        List<Post> posts = postPage.getContent();
        computeAndStoreDepths(posts);

        Map<UUID, Long> likeCounts = postRepository.countLikesForPosts(posts).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result.get("postUuid"),
                        result -> (Long) result.get("likeCount")));

        Set<UUID> likedPostUuids = likeRepository.findLikedPostUuidsByUserAndPosts(currentUser, posts);

        List<PostResponseDTO> postResponseDTOList = posts.stream()
                .map(post -> mapPostToResponseDTO(post, currentUser, 1, likeCounts, likedPostUuids))
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
    @Transactional
    public PagedResponseDTO<PostResponseDTO> getFeedPosts(Pageable pageable, UserDetails currentUserDetails) {
        User currentUser = getCurrentUserOrNull(currentUserDetails);

        if (currentUser == null) {
            return getAllTopLevelPosts(pageable, null);
        }

        // Create a new Pageable object without any sorting information
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        // Step 1: Get the sorted and paginated list of Post UUIDs using the unsorted pageable.
        Page<UUID> postUuidsPage = postRepository.findFeedPostUuidsForUser(currentUser, unsortedPageable); // Use the new unsortedPageable
        List<UUID> postUuids = postUuidsPage.getContent();

        if (postUuids.isEmpty()) {
            return new PagedResponseDTO<>(Collections.emptyList(), postUuidsPage.getNumber(), postUuidsPage.getTotalPages(), postUuidsPage.getTotalElements(), postUuidsPage.isLast());
        }

        // Step 2: Fetch the full Post entities for the retrieved UUIDs.
        List<Post> posts = postRepository.findByUuidIn(postUuids);

        // Re-sort the posts in memory to match the order from the first query
        posts.sort(Comparator.comparing(p -> postUuids.indexOf(p.getUuid())));

        computeAndStoreDepths(posts);

        log.debug("Fetched {} feed posts for user {}", posts.size(), currentUser.getUsername());

        // ... the rest of the method remains the same ...
        Map<UUID, Long> likeCounts = postRepository.countLikesForPosts(posts).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result.get("postUuid"),
                        result -> (Long) result.get("likeCount")));

        Set<UUID> likedPostUuids = likeRepository.findLikedPostUuidsByUserAndPosts(currentUser, posts);

        List<PostResponseDTO> postResponseDTOList = posts.stream()
                .map(post -> mapPostToResponseDTO(post, currentUser, 1, likeCounts, likedPostUuids))
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                postResponseDTOList,
                postUuidsPage.getNumber(),
                postUuidsPage.getTotalPages(),
                postUuidsPage.getTotalElements(),
                postUuidsPage.isLast()
        );
    }

    @Override
    @Transactional
    public PagedResponseDTO<PostResponseDTO> getPostsByUser(UUID userUuid, Pageable pageable, UserDetails currentUserDetails) {
        User postAuthor = (User) userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        Page<Post> postPage = postRepository.getPostsByAuthor(postAuthor.getUuid(), pageable);
        log.debug("Fetched {} posts for user {}", postPage.getNumberOfElements(), postAuthor.getUsername());

        List<Post> posts = postPage.getContent();
        computeAndStoreDepths(posts);

        User currentUser = getCurrentUserOrNull(currentUserDetails);

        Map<UUID, Long> likeCounts = postRepository.countLikesForPosts(posts).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result.get("postUuid"),
                        result -> (Long) result.get("likeCount")));

        Set<UUID> likedPostUuids = (currentUser != null)
                ? likeRepository.findLikedPostUuidsByUserAndPosts(currentUser, posts)
                : Collections.emptySet();

        List<PostResponseDTO> postResponseDTOList = posts.stream()
                .map(post -> mapPostToResponseDTO(post, currentUser, 1, likeCounts, likedPostUuids))
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                postResponseDTOList,
                postPage.getNumber(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isLast()
        );
    }

    // Add this helper method to your PostServiceImpl class
    private List<Post> collectAllPostsIncludingComments(Post post) {
        List<Post> allPosts = new ArrayList<>();
        allPosts.add(post);

        if (post.getComments() != null && !post.getComments().isEmpty()) {
            for (Post comment : post.getComments()) {
                allPosts.addAll(collectAllPostsIncludingComments(comment));
            }
        }

        return allPosts;
    }


    @Override
    @Transactional
    public PostResponseDTO getPostByUuid(UUID uuid, UserDetails currentUserDetails) {
        Post post = postRepository.findByUuid(uuid)
                .map(p -> (Post) p) // Cast from Object to Post
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with UUID: " + uuid));

        User currentUser = getCurrentUserOrNull(currentUserDetails);

        // ✅ Collect ALL posts (the main post + all its nested comments) into a flat list.
        List<Post> allPosts = collectAllPostsIncludingComments(post);

        // ✅ Run the depth calculation on the entire collection of posts.
        computeAndStoreDepths(allPosts);


        // ✅ Calculate like counts for all the posts in the thread using a for-loop.
        List<Map<String, Object>> likeCountResults = postRepository.countLikesForPosts(allPosts);
        Map<UUID, Long> likeCounts = new HashMap<>();
        for (Map<String, Object> result : likeCountResults) {
            UUID postUuid = (UUID) result.get("postUuid");
            Long count = (Long) result.get("likeCount");
            likeCounts.put(postUuid, count);
        }

        // ✅ Calculate the liked status for all posts in the thread.
        Set<UUID> likedPostUuids = (currentUser != null)
                ? likeRepository.findLikedPostUuidsByUserAndPosts(currentUser, allPosts)
                : Collections.emptySet();

        // ✅ Correctly call the mapping function.
        return mapPostToResponseDTO(post, currentUser, 1, likeCounts, likedPostUuids);
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
    @Transactional
    public void updatePost(UserDetails currentUser, UUID postUuid, PostUpdateDTO postUpdateDTO) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().getUuid().equals(user.getUuid())) {
            log.warn("ACCESS DENIED: User '{}' attempted to update post '{}' owned by '{}'",
                    user.getUsername(), postUuid, post.getAuthor().getUsername());
            throw new AccessDeniedException("You are not authorized to update this post");
        }
        post.setContent(postUpdateDTO.getContent());

        List<String> oldImgUrls = new ArrayList<>(post.getImageUrls()); // Create a copy to avoid concurrent modification issues

        List<String> existingImgUrls = postUpdateDTO.getExistingImages();
        if (existingImgUrls == null) {
            existingImgUrls = new ArrayList<>();
        }


        // If any image url that is in oldImgUrls is not in existingImgUrls, we delete it
        if (!oldImgUrls.isEmpty()) {
            for (String oldImgUrl : oldImgUrls) {
                if (!existingImgUrls.contains(oldImgUrl)) {
                    try {
                        cloudinaryService.delete(oldImgUrl);
                        log.info("Deleted image from cloudinary: {}", oldImgUrl);
                    } catch (IOException e) {
                        log.error("Failed to delete image from cloudinary: {}", oldImgUrl, e);
                    }
                }
            }
        }


        if (postUpdateDTO.getNewImages() != null && !postUpdateDTO.getNewImages().isEmpty()) {

            for (String base64ImgString : postUpdateDTO.getNewImages()) {
                String imgUrl = null;
                try {
                    imgUrl = cloudinaryService.upload(base64ImgString);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (imgUrl != null) {
                    existingImgUrls.add(imgUrl);
                } else {
                    log.warn("Failed to upload image, skipping");
                }
            }
        }

        post.setImageUrls(existingImgUrls);
        post.setUpdatedAt(Instant.now()); //Instant.now() for better precision
        Post updatedPost = postRepository.save(post);
        log.info("Post with UUID: {} updated successfully by user: {}", postUuid, user.getUsername());


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
        notificationService.createNotification(user, post.getAuthor(), NotificationType.POST_LIKE, post.getUuid(),null);

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
    @Transactional
    public PagedResponseDTO<PostResponseDTO> getLikedPostsByUser(UUID userUuid, Pageable pageable) {
        User user = (User) userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with uuid: " + userUuid));

        Page<Like> likedPostsPage = likeRepository.findByUser(user, pageable);

        // Extract the list of posts from the likes
        List<Post> posts = likedPostsPage.getContent().stream()
                .map(Like::getPost)
                .collect(Collectors.toList());
        computeAndStoreDepths(posts);

        // Fetch like counts for these posts in bulk
        Map<UUID, Long> likeCounts = postRepository.countLikesForPosts(posts).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result.get("postUuid"),
                        result -> (Long) result.get("likeCount")));

        // Fetch liked statuses for these posts in bulk
        // Since these are all liked posts by the user, all of them will be in the set.
        Set<UUID> likedPostUuids = posts.stream()
                .map(Post::getUuid)
                .collect(Collectors.toSet());

        // Use the new mapping method with the fetched data
        List<PostResponseDTO> postResponseDTOs = posts.stream()
                .map(post -> mapPostToResponseDTO(post, user, 1, likeCounts, likedPostUuids))
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                postResponseDTOs,
                likedPostsPage.getNumber(),
                likedPostsPage.getTotalPages(),
                likedPostsPage.getTotalElements(),
                likedPostsPage.isLast()
        );
    }

    @Override
    public String increaseShareCount(UUID postUuid) {

        Post post = postRepository.findByUuid(postUuid).orElseThrow(()->new ResourceNotFoundException("Post not found with UUID: " + postUuid));

        post.setShareCount(post.getShareCount() + 1);
        postRepository.save(post);

        return "incremented shareCount to " + post.getShareCount();
    }

    @Override
    @Transactional
    public PagedResponseDTO<PostResponseDTO> getAllShorts(Pageable pageable, UserDetails currentUserDetails) {
        Page<Post> postPage = postRepository.findAllByPostType(Post.PostType.VIDEO_SHORT, pageable);
        log.debug("Fetched {} shorts from page {}", postPage.getNumberOfElements(), pageable.getPageNumber());

        List<Post> posts = postPage.getContent();
        computeAndStoreDepths(posts);

        User currentUser = getCurrentUserOrNull(currentUserDetails);

        Map<UUID, Long> likeCounts = postRepository.countLikesForPosts(posts).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result.get("postUuid"),
                        result -> (Long) result.get("likeCount")));

        Set<UUID> likedPostUuids = (currentUser != null)
                ? likeRepository.findLikedPostUuidsByUserAndPosts(currentUser, posts)
                : Collections.emptySet();

        List<PostResponseDTO> postResponseDTOList = posts.stream()
                .map(post -> mapPostToResponseDTO(post, currentUser, 1, likeCounts, likedPostUuids))
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                postResponseDTOList,
                postPage.getNumber(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isLast()
        );
    }

    // --- HELPER METHODS ---

    private User getCurrentUserOrNull(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }


    private PostResponseDTO mapPostToResponseDTO(Post post, User currentUser, int recursionDepth, Map<UUID, Long> likeCounts, Set<UUID> likedPostUuids) {
        if (post == null) return null;

        PostResponseDTO dto = new PostResponseDTO();

        dto.setPostUuid(post.getUuid());
        dto.setContent(post.getContent());
        dto.setImageUrls(post.getImageUrls());
        dto.setVideoUrl(post.getVideoUrl());
        dto.setPostType(PostResponseDTO.PostType.valueOf(post.getPostType().name()));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setAuthorUsername(post.getAuthor().getUsername());
        dto.setAuthorUuid(post.getAuthor().getUuid());
        dto.setShareCount(post.getShareCount());

        // Use the UUID of the post to look up its depth in the map
        dto.setDepth(post.getDepth() != null ? post.getDepth() : 0);

        if (post.getAuthor().getUserProfile() != null && post.getAuthor().getUserProfile().getProfilePictureUrl() != null) {
            dto.setAuthorProfilePictureUrl(post.getAuthor().getUserProfile().getProfilePictureUrl());
        } else {
            dto.setAuthorProfilePictureUrl("https://res.cloudinary.com/dvsutdpx2/image/upload/v1732181213/ryi6ouf4e0mwcgz1tcxx.png");
        }

        dto.setLikedByCurrentUser(likedPostUuids.contains(post.getUuid()));
        dto.setLikeCount(likeCounts.getOrDefault(post.getUuid(), 0L).intValue());

        if (post.getParentPost() != null) {
            dto.setParentPostUuid(post.getParentPost().getUuid());
        }

        dto.setCommentCount(countNestedComments(post));


        List<Post> commentList = post.getComments();
        if (recursionDepth > 0 && commentList != null && !commentList.isEmpty()) {
            List<PostResponseDTO> commentDTOs = new ArrayList<>();
            for (Post comment : commentList) {
                commentDTOs.add(mapPostToResponseDTO(comment, currentUser, recursionDepth - 1, likeCounts, likedPostUuids));
            }
            dto.setComments(commentDTOs);
        }

        return dto;
    }

    private long countNestedComments(Post post) {
        if (post == null || post.getComments() == null || post.getComments().isEmpty()) {
            return 0;
        }

        long count = post.getComments().size();
        for (Post comment : post.getComments()) {
            count += countNestedComments(comment);
        }
        return count;
    }
}