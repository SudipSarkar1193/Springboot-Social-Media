package com.SSarkar.Xplore.controller;

import com.SSarkar.Xplore.dto.follow.FollowerDTO;
import com.SSarkar.Xplore.service.contract.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{uuid}")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity< HashMap<String,String>> followUser(@PathVariable UUID uuid, @AuthenticationPrincipal UserDetails currentUser) {
        String msg = followService.followUser(uuid, currentUser);

        HashMap<String,String> map = new HashMap<>();
        map.put("message",msg);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/followers")
    public ResponseEntity<List<FollowerDTO>> getFollowers(@PathVariable UUID uuid) {
        return ResponseEntity.ok(followService.getFollowers(uuid));
    }

    @GetMapping("/following")
    public ResponseEntity<List<FollowerDTO>> getFollowing(@PathVariable UUID uuid) {
        return ResponseEntity.ok(followService.getFollowing(uuid));
    }

}