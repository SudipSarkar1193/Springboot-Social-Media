package com.SSarkar.Xplore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CommentRequestDTO {

    @NotBlank(message = "Comment content cannot be blank")
    @Size(min = 1, max = 320, message = "Content must be between 1 and 320 characters")
    private String content;

    // Optional: If we want to allow image URLs in comments
    private List<String> imageUrls;
}