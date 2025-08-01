package com.SSarkar.Xplore.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class PostUpdateDTO {
    @NotBlank(message = "Post content cannot be empty.")
    @Size(max = 1000, message = "Post content cannot exceed 1000 characters.")
    private String content;

    // list of URLs for existingImages that should be kept.
    private List<String> existingImages;

    // list of new images encoded as Base64 strings.
    private List<String> newImages;

    @NotBlank(message = "Must provide user uuid")
    private UUID authorUUid ;
}
