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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDTO {

    // Duplicating it here for clarity, or you could reference the entity's enum.
    public enum PostType {
        TEXT_IMAGE,
        VIDEO_SHORT
    }

    private UUID postUuid;
    private String content;
    private List<String> imageUrls;

    // Field for video URL (if applicable)
    private String videoUrl;

    // Field to indicate the type of post
    private PostType postType;

    private Instant createdAt;
    private Instant updatedAt;
    private String authorUsername;
    private UUID authorUuid;
    private String authorProfilePictureUrl;

    // --- Fields for Comments ---
    private UUID parentPostUuid;
    private List<PostResponseDTO> comments;
    private long commentCount;
    private long shareCount;
    private int depth;

    // --- Fields for Likes ---
    private int likeCount;
    private boolean isLikedByCurrentUser;
}