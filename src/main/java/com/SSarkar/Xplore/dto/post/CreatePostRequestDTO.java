package com.SSarkar.Xplore.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CreatePostRequestDTO {

    private String content;
    private List<String> imageUrls;

}