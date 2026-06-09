package com.crud.security;

import com.crud.dto.NoteResponse;
import com.crud.dto.ProfileResponse;
import com.crud.dto.RoleResponse;
import com.crud.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SanitizedJsonResponses {

    private final ApiOutputSanitizer sanitizer;

    public ResponseEntity<UserResponse> ok(UserResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public ResponseEntity<UserResponse> created(UserResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public ResponseEntity<Page<UserResponse>> okUsers(Page<UserResponse> page) {
        return ResponseEntity.ok(page.map(this::sanitize));
    }

    public ResponseEntity<NoteResponse> ok(NoteResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public ResponseEntity<NoteResponse> created(NoteResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public ResponseEntity<Page<NoteResponse>> okNotes(Page<NoteResponse> page) {
        return ResponseEntity.ok(page.map(this::sanitize));
    }

    public ResponseEntity<ProfileResponse> ok(ProfileResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public ResponseEntity<ProfileResponse> created(ProfileResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public ResponseEntity<Page<ProfileResponse>> okProfiles(Page<ProfileResponse> page) {
        return ResponseEntity.ok(page.map(this::sanitize));
    }

    public ResponseEntity<RoleResponse> ok(RoleResponse body) {
        return ResponseEntity.ok(sanitize(body));
    }

    public ResponseEntity<RoleResponse> created(RoleResponse body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(body));
    }

    public ResponseEntity<Page<RoleResponse>> okRoles(Page<RoleResponse> page) {
        return ResponseEntity.ok(page.map(this::sanitize));
    }

    public ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> ok() {
        return ResponseEntity.ok().build();
    }

    private UserResponse sanitize(UserResponse response) {
        return new UserResponse(
                response.id(),
                sanitizer.sanitize(response.name()),
                sanitizer.sanitize(response.email()),
                response.age(),
                response.notificationDeliveryStatus(),
                response.createdAt()
        );
    }

    private NoteResponse sanitize(NoteResponse response) {
        return new NoteResponse(
                response.id(),
                sanitizer.sanitize(response.content()),
                response.createdAt(),
                response.updatedAt()
        );
    }

    private ProfileResponse sanitize(ProfileResponse response) {
        return new ProfileResponse(
                response.id(),
                response.userId(),
                sanitizer.sanitize(response.phone()),
                sanitizer.sanitize(response.address()),
                response.createdAt(),
                response.updatedAt()
        );
    }

    private RoleResponse sanitize(RoleResponse response) {
        return new RoleResponse(
                response.id(),
                sanitizer.sanitize(response.name()),
                response.createdAt(),
                response.updatedAt()
        );
    }
}
