package com.SSarkar.Xplore.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String fullName;

    @NotBlank(message = "User-name cannot be empty.")
    @Size(max = 50, message = "Username cannot exceed 50 characters.")
    private String username;

    @Size(max =320 ,message = "Bio cannot exceed 320 characters.")
    private String bio;

    private String profileImageUrl;
}
