package com.crud.controller;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.security.JsonResponses;
import com.crud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return JsonResponses.created(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUserById(@PathVariable Long id) {
        return JsonResponses.ok(userService.findUserById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAllUsers(Pageable pageable) {
        return JsonResponses.okUsers(userService.findAllUsers(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return JsonResponses.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return JsonResponses.noContent();
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> findUserByEmail(@RequestParam String email) {
        return JsonResponses.ok(userService.findUserByEmail(email));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam String email, Pageable pageable) {
        return JsonResponses.okUsers(userService.searchUsersByEmail(email, pageable));
    }
}
