package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.post.*;
import com.SSarkar.Xplore.service.contract.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @Valid @RequestBody CreatePostRequestDTO createPostRequest,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.info("Creating post for user: {}", currentUser.getUsername());
        log.debug("Post creation request: {}", createPostRequest);


        PostResponseDTO newPost = postService.createPost(createPostRequest, currentUser);
        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    @GetMapping("/feed")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getTopLevelPosts(
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser) {
        PagedResponseDTO<PostResponseDTO> posts = postService.getFeedPosts(pageable, currentUser);
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

    @PostMapping("/{parentPostUuid}/comments")
    public ResponseEntity<PostResponseDTO> createComment(
            @PathVariable UUID parentPostUuid,
            @Valid @RequestBody CommentRequestDTO commentRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        PostResponseDTO newComment = postService.addCommentToPost(parentPostUuid, commentRequest, currentUser);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @GetMapping("/user/{uuid}")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getUserPosts(
            @PathVariable UUID uuid,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        PagedResponseDTO<PostResponseDTO> posts = postService.getPostsByUser(uuid, pageable, currentUser);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/likes/{uuid}")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getLikedPosts(
            @PathVariable UUID uuid,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {
        PagedResponseDTO<PostResponseDTO> posts = postService.getLikedPostsByUser(uuid, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/following")
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getFollowingPosts(
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ){
        PagedResponseDTO<PostResponseDTO> posts = postService.getAllFollowingPost(pageable, currentUser);
        return ResponseEntity.ok(posts);
    }
}