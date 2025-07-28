package com.SSarkar.Xplore.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class PostResponseDTO {

    private UUID postUuid;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private String authorUsername;
    private UUID authorUuid;

}