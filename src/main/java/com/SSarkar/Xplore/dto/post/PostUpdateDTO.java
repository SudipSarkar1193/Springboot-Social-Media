package com.SSarkar.Xplore.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class PostUpdateDTO {
    @NotBlank(message = "Post content cannot be blank")
    @Size(min = 1, max = 320, message = "Content must be between 1 and 320 characters")
    private String content;

    private List<String> imageUrls = new ArrayList<>();

    @NotBlank(message = "Must provide user uuid")
    private UUID authorUUid ;
}
