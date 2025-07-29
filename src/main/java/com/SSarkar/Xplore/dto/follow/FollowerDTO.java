package com.SSarkar.Xplore.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowerDTO {
    private UUID uuid;
    private String username;
    private String profilePictureUrl;
}