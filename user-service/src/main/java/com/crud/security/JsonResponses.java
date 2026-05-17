package com.crud.security;

import com.crud.dto.NoteResponse;
import com.crud.dto.ProfileResponse;
import com.crud.dto.RoleResponse;
import com.crud.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class JsonResponses {

    private JsonResponses() {
    }

    public static ResponseEntity<UserResponse> ok(UserResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public static ResponseEntity<UserResponse> created(UserResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public static ResponseEntity<Page<UserResponse>> okUsers(Page<UserResponse> page) {
        return ResponseEntity.ok(page.map(JsonResponses::sanitize));
    }

    public static ResponseEntity<NoteResponse> ok(NoteResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public static ResponseEntity<NoteResponse> created(NoteResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public static ResponseEntity<Page<NoteResponse>> okNotes(Page<NoteResponse> page) {
        return ResponseEntity.ok(page.map(JsonResponses::sanitize));
    }

    public static ResponseEntity<ProfileResponse> ok(ProfileResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public static ResponseEntity<ProfileResponse> created(ProfileResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public static ResponseEntity<Page<ProfileResponse>> okProfiles(Page<ProfileResponse> page) {
        return ResponseEntity.ok(page.map(JsonResponses::sanitize));
    }

    public static ResponseEntity<RoleResponse> ok(RoleResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public static ResponseEntity<RoleResponse> created(RoleResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public static ResponseEntity<Page<RoleResponse>> okRoles(Page<RoleResponse> page) {
        return ResponseEntity.ok(page.map(JsonResponses::sanitize));
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static ResponseEntity<Void> ok() {
        return ResponseEntity.ok().build();
    }

    private static UserResponse sanitize(UserResponse response) {
        return new UserResponse(
                response.id(),
                ApiOutputSanitizer.sanitize(response.name()),
                ApiOutputSanitizer.sanitize(response.email()),
                response.age(),
                response.createdAt()
        );
    }

    private static NoteResponse sanitize(NoteResponse response) {
        return new NoteResponse(
                response.id(),
                ApiOutputSanitizer.sanitize(response.content()),
                response.createdAt(),
                response.updatedAt()
        );
    }

    private static ProfileResponse sanitize(ProfileResponse response) {
        return new ProfileResponse(
                response.id(),
                response.userId(),
                ApiOutputSanitizer.sanitize(response.phone()),
                ApiOutputSanitizer.sanitize(response.address()),
                response.createdAt(),
                response.updatedAt()
        );
    }

    private static RoleResponse sanitize(RoleResponse response) {
        return new RoleResponse(
                response.id(),
                ApiOutputSanitizer.sanitize(response.name()),
                response.createdAt(),
                response.updatedAt()
        );
    }
}
