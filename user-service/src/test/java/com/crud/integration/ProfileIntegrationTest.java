package com.crud.integration;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
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
class ProfileIntegrationTest extends AbstractIntegrationTest {

    private Long createTestUser() {
        UserRequest request = new UserRequest("Profile User", uniqueEmail("profile-user"), 30);
        HttpEntity<UserRequest> entity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, entity, UserResponse.class);
        Assertions.assertNotNull(response.getBody());
        return response.getBody().id();
    }

    @Test
    void createAndGetProfile_ShouldWork() {
        Long userId = createTestUser();

        ProfileRequest request = new ProfileRequest("+79991234567", "Moscow, Tverskaya 1");
        HttpEntity<ProfileRequest> createEntity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<ProfileResponse> createResponse = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.POST, createEntity, ProfileResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().phone()).isEqualTo("+79991234567");
        assertThat(createResponse.getBody().address()).isEqualTo("Moscow, Tverskaya 1");
        assertThat(createResponse.getBody().userId()).isEqualTo(userId);

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<ProfileResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.GET, getEntity, ProfileResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(getResponse.getBody());
        assertThat(getResponse.getBody().phone()).isEqualTo("+79991234567");
    }

    @Test
    void updateProfile_ShouldChangeFields() {
        Long userId = createTestUser();

        ProfileRequest createReq = new ProfileRequest("+000", "Old Address");
        HttpEntity<ProfileRequest> createEntity = new HttpEntity<>(createReq, authHeaders());
        restTemplate.exchange(baseUrl() + "/profiles/user/" + userId, HttpMethod.POST, createEntity, ProfileResponse.class);

        ProfileRequest updateReq = new ProfileRequest("+111222333", "New Address");
        HttpEntity<ProfileRequest> updateEntity = new HttpEntity<>(updateReq, authHeaders());
        ResponseEntity<ProfileResponse> updateResponse = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.PUT, updateEntity, ProfileResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(updateResponse.getBody());
        assertThat(updateResponse.getBody().phone()).isEqualTo("+111222333");
        assertThat(updateResponse.getBody().address()).isEqualTo("New Address");
    }

    @Test
    void deleteProfile_ShouldReturn204() {
        Long userId = createTestUser();

        ProfileRequest request = new ProfileRequest("+999", "Delete Address");
        HttpEntity<ProfileRequest> createEntity = new HttpEntity<>(request, authHeaders());
        restTemplate.exchange(baseUrl() + "/profiles/user/" + userId, HttpMethod.POST, createEntity, ProfileResponse.class);

        HttpEntity<Void> deleteEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/profiles/user/" + userId, HttpMethod.DELETE, deleteEntity, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void findAllProfiles_ShouldReturnPage() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/profiles?page=0&size=10", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }
}
