package com.crud.controller;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> createProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> findProfileByUserId(@PathVariable Long userId) {
        ProfileResponse response = profileService.findProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProfileResponse>> findAllProfiles(Pageable pageable) {
        Page<ProfileResponse> page = profileService.findAllProfiles(pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) {
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
