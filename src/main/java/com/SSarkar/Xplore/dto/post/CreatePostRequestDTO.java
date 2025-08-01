package com.SSarkar.Xplore.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CreatePostRequestDTO {


    @Size(max = 320, message = "Content must be between 1 and 320 characters")
    private String content;
    private List<String> imageUrls;

}