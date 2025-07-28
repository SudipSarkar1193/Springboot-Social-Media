package com.SSarkar.Xplore.dto.auth;

import lombok.Data;
import java.util.UUID;

@Data
public class UserResponseDTO {

    private UUID uuid;
    private String username;
    private String email;
    private String profilePictureUrl;

}