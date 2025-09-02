package com.SSarkar.Xplore.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CommentRequestDTO {

    @Size(min = 0, max = 320, message = "Content must be between 1 and 320 characters")
    private String content;

}