package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.post.*;
import com.SSarkar.Xplore.service.contract.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    // To create a lock that only allows one thread at a time.
    private final Semaphore uploadLock = new Semaphore(1);

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createPost( // Change return type to ResponseEntity<?>
                                         @RequestPart(value = "content", required = false) String content,
                                         @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                         @RequestPart(value = "video", required = false) MultipartFile video,
                                         @AuthenticationPrincipal UserDetails currentUser) {


        final long LARGE_UPLOAD_THRESHOLD = 40 * 1024 * 1024;
        // Check if the upload is for a large video file
        boolean isLargeUpload = (video != null && !video.isEmpty() && video.getSize() > LARGE_UPLOAD_THRESHOLD);

        if (isLargeUpload) {
            // Try to acquire the lock without waiting.
            if (!uploadLock.tryAcquire()) {
                // If the lock cannot be acquired, it means another large upload is in progress.
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Server is busy processing another large upload. Please try again in a few moments.");
                return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
            }
        }

        try {
            log.info("Creating post for user: {}", currentUser.getUsername());
            if (images != null) {
                log.debug("Post creation images RequestPart: {}", Arrays.toString(images.toArray()));
            }
            if (video != null) {
                log.debug("Post creation video RequestPart: {}", video.getOriginalFilename());
            }

            CreatePostRequestDTO createPostRequest = new CreatePostRequestDTO();
            createPostRequest.setContent(content);

            PostResponseDTO newPost = postService.createPost(createPostRequest, images, video, currentUser);
            return new ResponseEntity<>(newPost, HttpStatus.CREATED);

        } finally {
            // IMPORTANT: Release the lock in a finally block !!!!!
            // This ensures the lock is freed EVEN IF the upload fails.
            if (isLargeUpload) {
                uploadLock.release();
            }
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getTopLevelPosts(
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        PagedResponseDTO<PostResponseDTO> posts = postService.getFeedPosts(pageable, currentUser);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/following")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getFollowingPosts(
            @PageableDefault(size = 10, page = 0, sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser
    ){
        log.info("Following posts request: {}", currentUser.getUsername());
        PagedResponseDTO<PostResponseDTO> posts = postService.getAllFollowingPost(pageable, currentUser);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PostResponseDTO> getPostByUuid(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserDetails currentUser) {
        PostResponseDTO post = postService.getPostByUuid(uuid, currentUser);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserDetails currentUser) {
        postService.deletePost(uuid, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<HashMap<String, String>> updatePost(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestBody PostUpdateDTO postUpdateDTO
    ){
        postService.updatePost(currentUser,uuid,postUpdateDTO);
        HashMap<String, String> map = new HashMap<>();
        map.put("message", "Post updated successfully!!");
        return ResponseEntity.ok(map);
    }

    @PutMapping("/{uuid}/increase-share-count")
    public ResponseEntity<HashMap<String, String>> increaseShareCount(
            @PathVariable UUID uuid
    ){
        String msg = postService.increaseShareCount(uuid);
        HashMap<String, String> map = new HashMap<>();
        map.put("message", msg);
        return ResponseEntity.ok(map);
    }

    @PostMapping("/{postUuid}/like")
    public ResponseEntity<HashMap<String, String>> likePost(
            @PathVariable UUID postUuid,
            @AuthenticationPrincipal UserDetails currentUser) {
        String msg = postService.likePost(postUuid, currentUser);
        HashMap<String, String> map = new HashMap<>();
        map.put("message", msg);
        return ResponseEntity.ok(map);
    }

    @PostMapping(value = "/{parentPostUuid}/comments", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Object> createComment(
            @PathVariable UUID parentPostUuid,
            @RequestPart("commentRequest") @Valid CommentRequestDTO commentRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails currentUser) {
        try{
            PostResponseDTO newComment = postService.addCommentToPost(parentPostUuid, commentRequest, images, currentUser);
            return new ResponseEntity<>(newComment, HttpStatus.CREATED);
        } catch (Exception e) {
            String errorMsg = "Error creating comment: " + e.getMessage();
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("error", errorMsg);
            return new ResponseEntity<>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{uuid}")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getUserPosts(
            @PathVariable UUID uuid,
            @PageableDefault(size = 10, page = 0, sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        PagedResponseDTO<PostResponseDTO> posts = postService.getPostsByUser(uuid, pageable, currentUser);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/likes/{uuid}")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getLikedPosts(
            @PathVariable UUID uuid,
            @PageableDefault(size = 10, page = 0, sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponseDTO<PostResponseDTO> posts = postService.getLikedPostsByUser(uuid, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/shorts")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getShorts(
            @PageableDefault(size = 10, page = 0, sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        PagedResponseDTO<PostResponseDTO> posts = postService.getAllShorts(pageable, currentUser);
        return ResponseEntity.ok(posts);
    }


}