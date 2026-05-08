package com.crud.integration;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.SAME_THREAD)
class UserIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createUser_ShouldReturn201AndGetById() {
        String email = uniqueEmail("ivan");

        UserRequest request = new UserRequest("Ivan Petrov", email, 28);
        HttpEntity<UserRequest> createEntity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<UserResponse> createResponse = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, createEntity, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("Ivan Petrov");
        assertThat(created.email()).isEqualTo(email);
        assertThat(created.id()).isNotNull();

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<UserResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/users/" + created.id(), HttpMethod.GET, getEntity, UserResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().email()).isEqualTo(email);
    }

    @Test
    void updateUser_ShouldChangeFields() {
        String oldEmail = uniqueEmail("old");
        UserRequest createReq = new UserRequest("Old Name", oldEmail, 20);
        HttpEntity<UserRequest> createEntity = new HttpEntity<>(createReq, authHeaders());
        ResponseEntity<UserResponse> created = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, createEntity, UserResponse.class);
        Assertions.assertNotNull(created.getBody());
        Long id = created.getBody().id();

        String newEmail = uniqueEmail("new");
        UserRequest updateReq = new UserRequest("New Name", newEmail, 35);
        HttpEntity<UserRequest> updateEntity = new HttpEntity<>(updateReq, authHeaders());
        ResponseEntity<UserResponse> updateResponse = restTemplate.exchange(
                baseUrl() + "/users/" + id, HttpMethod.PUT, updateEntity, UserResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(updateResponse.getBody());
        assertThat(updateResponse.getBody().name()).isEqualTo("New Name");
        assertThat(updateResponse.getBody().email()).isEqualTo(newEmail);
        assertThat(updateResponse.getBody().age()).isEqualTo(35);
    }

    @Test
    void deleteUser_ShouldReturn204() {
        String email = uniqueEmail("delete");
        UserRequest request = new UserRequest("To Delete", email, 40);
        HttpEntity<UserRequest> createEntity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<UserResponse> created = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, createEntity, UserResponse.class);
        Assertions.assertNotNull(created.getBody());
        Long id = created.getBody().id();

        HttpEntity<Void> deleteEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/users/" + id, HttpMethod.DELETE, deleteEntity, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void findAllUsers_ShouldReturnPage() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/users?page=0&size=5", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }

    @Test
    void findUserByEmail_ShouldReturnUser() {
        String email = uniqueEmail("email-find");
        UserRequest request = new UserRequest("Email Test", email, 22);
        HttpEntity<UserRequest> createEntity = new HttpEntity<>(request, authHeaders());
        restTemplate.exchange(baseUrl() + "/users", HttpMethod.POST, createEntity, UserResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                usersByEmailUrl(email), HttpMethod.GET, entity, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo(email);
    }

    @Test
    void searchUsersByEmail_ShouldReturnPage() {
        String token = "searchgmail" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String email = token + "@gmail.com";
        UserRequest request = new UserRequest("Search Test", email, 30);
        HttpEntity<UserRequest> createEntity = new HttpEntity<>(request, authHeaders());
        restTemplate.exchange(baseUrl() + "/users", HttpMethod.POST, createEntity, UserResponse.class);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                usersSearchUrl(token), HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(email);
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturn400() {
        String email = uniqueEmail("dup");
        UserRequest request = new UserRequest("Dup1", email, 25);
        HttpEntity<UserRequest> entity = new HttpEntity<>(request, authHeaders());
        restTemplate.exchange(baseUrl() + "/users", HttpMethod.POST, entity, UserResponse.class);

        UserRequest request2 = new UserRequest("Dup2", email, 30);
        HttpEntity<UserRequest> entity2 = new HttpEntity<>(request2, authHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, entity2, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unauthorizedRequest_ShouldReturn401() {
        String email = uniqueEmail("noauth");
        UserRequest request = new UserRequest("No Auth", email, 20);
        HttpEntity<UserRequest> entity = new HttpEntity<>(request);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
