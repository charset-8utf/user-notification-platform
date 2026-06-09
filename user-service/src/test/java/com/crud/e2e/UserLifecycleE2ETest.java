package com.crud.e2e;

import com.crud.dto.*;
import com.crud.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.SAME_THREAD)
class UserLifecycleE2ETest extends AbstractIntegrationTest {

    private Long createTestUser(String name, String email, int age) {
        UserRequest request = new UserRequest(name, email, age);
        HttpEntity<UserRequest> entity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, entity, UserResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(response.getBody());
        return response.getBody().id();
    }

    private Long createTestUser(String name, int age) {
        return createTestUser(name, uniqueEmail("e2e-user"), age);
    }

    @Test
    void userCreationAndRetrieval_ShouldWork() {
        String primary = uniqueEmail("e2e");
        Long userId = createTestUser("E2E User", primary, 30);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<UserResponse> getResp = restTemplate.exchange(
                baseUrl() + "/users/" + userId, HttpMethod.GET, entity, UserResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(getResp.getBody());
        assertThat(getResp.getBody().name()).isEqualTo("E2E User");

        ResponseEntity<UserResponse> byEmailResp = restTemplate.exchange(
                usersByEmailUrl(primary), HttpMethod.GET, entity, UserResponse.class);
        assertThat(byEmailResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(byEmailResp.getBody());
        assertThat(byEmailResp.getBody().id()).isEqualTo(userId);

        String searchKey = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String updatedEmail = "upd" + searchKey + "@integration.local";
        UserRequest updateReq = new UserRequest("Updated", updatedEmail, 35);
        HttpEntity<UserRequest> updateEntity = new HttpEntity<>(updateReq, authHeaders());
        ResponseEntity<UserResponse> updateResp = restTemplate.exchange(
                baseUrl() + "/users/" + userId, HttpMethod.PUT, updateEntity, UserResponse.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(updateResp.getBody());
        assertThat(updateResp.getBody().name()).isEqualTo("Updated");

        ResponseEntity<String> searchResp = restTemplate.exchange(
                usersSearchUrl(searchKey), HttpMethod.GET, entity, String.class);
        assertThat(searchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResp.getBody()).contains(updatedEmail);
    }

    @Test
    void profileLifecycle_ShouldWork() {
        Long userId = createTestUser("Profile User", 25);
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ProfileRequest createReq = new ProfileRequest("+79990001111", "Moscow");
        HttpEntity<ProfileRequest> createEntity = new HttpEntity<>(createReq, authHeaders());
        ResponseEntity<ProfileResponse> createResp = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.POST, createEntity, ProfileResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(createResp.getBody());
        assertThat(createResp.getBody().phone()).isEqualTo("+79990001111");

        ResponseEntity<ProfileResponse> getResp = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.GET, entity, ProfileResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(getResp.getBody());
        assertThat(getResp.getBody().address()).isEqualTo("Moscow");

        ProfileRequest updateReq = new ProfileRequest("+7999222333", "St. Petersburg");
        HttpEntity<ProfileRequest> updateEntity = new HttpEntity<>(updateReq, authHeaders());
        ResponseEntity<ProfileResponse> updateResp = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.PUT, updateEntity, ProfileResponse.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(updateResp.getBody());
        assertThat(updateResp.getBody().phone()).isEqualTo("+7999222333");

        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.DELETE, entity, Void.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void notesLifecycle_ShouldWork() {
        Long userId = createTestUser("Notes User", 28);
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        NoteRequest note1Req = new NoteRequest("Note 1");
        NoteRequest note2Req = new NoteRequest("Note 2");
        HttpEntity<NoteRequest> note1Entity = new HttpEntity<>(note1Req, authHeaders());
        HttpEntity<NoteRequest> note2Entity = new HttpEntity<>(note2Req, authHeaders());

        ResponseEntity<NoteResponse> note1Resp = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes", HttpMethod.POST, note1Entity, NoteResponse.class);
        ResponseEntity<NoteResponse> note2Resp = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes", HttpMethod.POST, note2Entity, NoteResponse.class);

        assertThat(note1Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(note2Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(note1Resp.getBody());
        Long note1Id = note1Resp.getBody().id();

        ResponseEntity<String> notesResp = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes?page=0&size=10",
                HttpMethod.GET, entity, String.class);
        assertThat(notesResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(notesResp.getBody()).contains("Note 1");
        assertThat(notesResp.getBody()).contains("Note 2");

        NoteRequest updateReq = new NoteRequest("Updated Note 1");
        HttpEntity<NoteRequest> updateEntity = new HttpEntity<>(updateReq, authHeaders());
        ResponseEntity<NoteResponse> updateResp = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes/" + note1Id,
                HttpMethod.PUT, updateEntity, NoteResponse.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(updateResp.getBody());
        assertThat(updateResp.getBody().content()).isEqualTo("Updated Note 1");

        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes/" + note1Id,
                HttpMethod.DELETE, entity, Void.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void rolesLifecycle_ShouldWork() {
        Long userId = createTestUser("Roles User", 32);
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        String roleName = uniqueRoleName("E2E_ROLE");
        RoleRequest createRoleReq = new RoleRequest(roleName);
        HttpEntity<RoleRequest> roleEntity = new HttpEntity<>(createRoleReq, authHeaders());
        ResponseEntity<RoleResponse> createRoleResp = restTemplate.exchange(
                baseUrl() + "/roles", HttpMethod.POST, roleEntity, RoleResponse.class);
        assertThat(createRoleResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(createRoleResp.getBody());
        Long roleId = createRoleResp.getBody().id();

        ResponseEntity<Void> assignResp = restTemplate.exchange(
                baseUrl() + "/roles/assign?userId=" + userId + "&roleId=" + roleId,
                HttpMethod.POST, entity, Void.class);
        assertThat(assignResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> removeResp = restTemplate.exchange(
                baseUrl() + "/roles/remove?userId=" + userId + "&roleId=" + roleId,
                HttpMethod.POST, entity, Void.class);
        assertThat(removeResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> deleteRoleResp = restTemplate.exchange(
                baseUrl() + "/roles/" + roleId, HttpMethod.DELETE, entity, Void.class);
        assertThat(deleteRoleResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
