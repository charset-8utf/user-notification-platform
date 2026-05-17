package com.crud.controller;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.security.JsonResponses;
import com.crud.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return JsonResponses.created(roleService.createRole(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> findRoleById(@PathVariable Long id) {
        return JsonResponses.ok(roleService.findRoleById(id));
    }

    @GetMapping
    public ResponseEntity<Page<RoleResponse>> findAllRoles(Pageable pageable) {
        return JsonResponses.okRoles(roleService.findAllRoles(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return JsonResponses.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return JsonResponses.noContent();
    }

    @PostMapping("/assign")
    public ResponseEntity<Void> assignRoleToUser(
            @RequestParam Long userId,
            @RequestParam Long roleId) {
        roleService.assignRoleToUser(userId, roleId);
        return JsonResponses.ok();
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> removeRoleFromUser(
            @RequestParam Long userId,
            @RequestParam Long roleId) {
        roleService.removeRoleFromUser(userId, roleId);
        return JsonResponses.ok();
    }
}
