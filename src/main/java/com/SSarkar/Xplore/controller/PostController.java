package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.CreatePostRequestDTO;
import com.SSarkar.Xplore.dto.PagedResponseDTO;
import com.SSarkar.Xplore.dto.PostResponseDTO;
import com.SSarkar.Xplore.service.contract.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Interview Insight: Getting the Current User
     * ---------------------------------------------
     * The @AuthenticationPrincipal annotation is a powerful Spring Security feature.
     * It automatically injects the currently authenticated user's principal (in our case, the UserDetails object)
     * into the method parameter. This is the standard, secure way to identify who is making the request.
     */
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @Valid @RequestBody CreatePostRequestDTO createPostRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        PostResponseDTO newPost = postService.createPost(createPostRequest, currentUser);
        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }


    /**
     * Interview Insight: Pagination with Pageable
     * ------------------------------------------------
     * We don't just return a List<PostResponseDTO>. Instead, we return a Page<PostResponseDTO>.
     *
     * Why? Returning a raw list is dangerous. If you have millions of posts, you'll crash your server
     * trying to load them all into memory.
     *
     * Pageable automatically handles request parameters like:
     * - ?page=0 (for the first page)
     * - ?size=10 (for 10 items per page)
     * - ?sort=createdAt,desc (to sort by creation date, descending)
     *
     * @PageableDefault provides a fallback if the client doesn't specify these params.
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<PostResponseDTO>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        PagedResponseDTO<PostResponseDTO> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PostResponseDTO> getPostByUuid(@PathVariable UUID uuid) {
        PostResponseDTO post = postService.getPostByUuid(uuid);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UserDetails currentUser) {
        postService.deletePost(uuid, currentUser);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content is standard for successful deletions
    }
}