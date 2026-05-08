package com.crud.integration;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.SAME_THREAD)
class RoleIntegrationTest extends AbstractIntegrationTest {

    private Long createTestUser() {
        UserRequest request = new UserRequest("Role User", uniqueEmail("role-user"), 30);
        HttpEntity<UserRequest> entity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, entity, UserResponse.class);
        Assertions.assertNotNull(response.getBody());
        return response.getBody().id();
    }

    @Test
    void createAndGetRole_ShouldWork() {
        String roleName = uniqueRoleName("TEST_ROLE");
        RoleRequest request = new RoleRequest(roleName);
        HttpEntity<RoleRequest> createEntity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<RoleResponse> createResponse = restTemplate.exchange(
                baseUrl() + "/roles", HttpMethod.POST, createEntity, RoleResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().name()).isEqualTo(roleName);

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<RoleResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/roles/" + createResponse.getBody().id(),
                HttpMethod.GET, getEntity, RoleResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(getResponse.getBody());
        assertThat(getResponse.getBody().name()).isEqualTo(roleName);
    }

    @Test
    void updateRole_ShouldChangeName() {
        String oldName = uniqueRoleName("OLD_ROLE");
        RoleRequest createReq = new RoleRequest(oldName);
        HttpEntity<RoleRequest> createEntity = new HttpEntity<>(createReq, authHeaders());
        ResponseEntity<RoleResponse> created = restTemplate.exchange(
                baseUrl() + "/roles", HttpMethod.POST, createEntity, RoleResponse.class);
        Assertions.assertNotNull(created.getBody());
        Long roleId = created.getBody().id();

        String newName = uniqueRoleName("NEW_ROLE");
        RoleRequest updateReq = new RoleRequest(newName);
        HttpEntity<RoleRequest> updateEntity = new HttpEntity<>(updateReq, authHeaders());
        ResponseEntity<RoleResponse> updateResponse = restTemplate.exchange(
                baseUrl() + "/roles/" + roleId, HttpMethod.PUT, updateEntity, RoleResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(updateResponse.getBody());
        assertThat(updateResponse.getBody().name()).isEqualTo(newName);
    }

    @Test
    void deleteRole_ShouldReturn204() {
        String roleName = uniqueRoleName("TO_DELETE_ROLE");
        RoleRequest request = new RoleRequest(roleName);
        HttpEntity<RoleRequest> createEntity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<RoleResponse> created = restTemplate.exchange(
                baseUrl() + "/roles", HttpMethod.POST, createEntity, RoleResponse.class);
        Assertions.assertNotNull(created.getBody());
        Long roleId = created.getBody().id();

        HttpEntity<Void> deleteEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/roles/" + roleId, HttpMethod.DELETE, deleteEntity, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void assignAndRemoveRole_ShouldWork() {
        Long userId = createTestUser();

        String roleName = uniqueRoleName("ASSIGN_TEST_ROLE");
        RoleRequest roleReq = new RoleRequest(roleName);
        HttpEntity<RoleRequest> roleEntity = new HttpEntity<>(roleReq, authHeaders());
        ResponseEntity<RoleResponse> roleResponse = restTemplate.exchange(
                baseUrl() + "/roles", HttpMethod.POST, roleEntity, RoleResponse.class);
        Assertions.assertNotNull(roleResponse.getBody());
        Long roleId = roleResponse.getBody().id();

        HttpEntity<Void> assignEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<Void> assignResponse = restTemplate.exchange(
                baseUrl() + "/roles/assign?userId=" + userId + "&roleId=" + roleId,
                HttpMethod.POST, assignEntity, Void.class);

        assertThat(assignResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpEntity<Void> removeEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<Void> removeResponse = restTemplate.exchange(
                baseUrl() + "/roles/remove?userId=" + userId + "&roleId=" + roleId,
                HttpMethod.POST, removeEntity, Void.class);

        assertThat(removeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void findAllRoles_ShouldReturnPage() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/roles?page=0&size=10", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }
}
