package com.SSarkar.Xplore.dto.user;

import lombok.Data;
import java.util.UUID;

@Data
public class UserResponseDTO {

    private UUID uuid;
    private String username;
    private String email;
    private String profilePictureUrl;
    private int followersCount;
    private int followingCount;
    private String fullName;
    private String bio ;

}