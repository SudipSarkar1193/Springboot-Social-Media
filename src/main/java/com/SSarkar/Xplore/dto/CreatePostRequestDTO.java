package com.SSarkar.Xplore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequestDTO {

    @NotBlank(message = "Post content cannot be blank")
    @Size(min = 1, max = 320, message = "Content must be between 1 and 320 characters")
    private String content;
    private List<String> imageUrls;
}