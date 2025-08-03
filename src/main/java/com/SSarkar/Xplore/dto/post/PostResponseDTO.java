package com.SSarkar.Xplore.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in the JSON response
public class PostResponseDTO {

    private UUID postUuid;
    private String content;
    private List<String> imageUrls;
    private Instant createdAt;
    private Instant updatedAt;
    private String authorUsername;
    private UUID authorUuid;
    private String authorProfilePictureUrl;

    // --- NEW Fields for Comments ---
    private UUID parentPostUuid;
    private List<PostResponseDTO> comments; // A list of nested comments
    private long commentCount;
    private long shareCount ;

    // --- NEW Fields for Likes ---
    private int likeCount;
    private boolean isLikedByCurrentUser;
}