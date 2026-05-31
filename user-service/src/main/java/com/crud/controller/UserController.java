package com.crud.controller;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.security.SanitizedJsonResponses;
import com.crud.service.UserService;
import com.platform.commons.audit.AuditLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SanitizedJsonResponses responses;

    @AuditLog(action = "USER_CREATE", resourceType = "user")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return responses.created(userService.createUser(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser(@AuthenticationPrincipal Jwt jwt) {
        return responses.ok(userService.findUserByUsername(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUserById(@PathVariable Long id) {
        return responses.ok(userService.findUserById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAllUsers(Pageable pageable) {
        return responses.okUsers(userService.findAllUsers(pageable));
    }

    @AuditLog(action = "USER_UPDATE", resourceType = "user")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return responses.ok(userService.updateUser(id, request));
    }

    @AuditLog(action = "USER_DELETE", resourceType = "user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return responses.noContent();
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> findUserByEmail(@RequestParam String email) {
        return responses.ok(userService.findUserByEmail(email));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam String email, Pageable pageable) {
        return responses.okUsers(userService.searchUsersByEmail(email, pageable));
    }
}
