package com.crud.controller;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.security.JsonResponses;
import com.crud.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> createProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileRequest request) {
        return JsonResponses.created(profileService.createProfile(userId, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> findProfileByUserId(@PathVariable Long userId) {
        return JsonResponses.ok(profileService.findProfileByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<Page<ProfileResponse>> findAllProfiles(Pageable pageable) {
        return JsonResponses.okProfiles(profileService.findAllProfiles(pageable));
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileRequest request) {
        return JsonResponses.ok(profileService.updateProfile(userId, request));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) {
        profileService.deleteProfile(userId);
        return JsonResponses.noContent();
    }
}
