package com.crud.integration;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
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
class NoteIntegrationTest extends AbstractIntegrationTest {

    private Long createTestUser() {
        UserRequest request = new UserRequest("Note User", uniqueEmail("note-user"), 25);
        HttpEntity<UserRequest> entity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.POST, entity, UserResponse.class);
        Assertions.assertNotNull(response.getBody());
        return response.getBody().id();
    }

    @Test
    void createAndGetNote_ShouldWork() {
        Long userId = createTestUser();

        NoteRequest request = new NoteRequest("Test note content");
        HttpEntity<NoteRequest> createEntity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<NoteResponse> createResponse = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes", HttpMethod.POST, createEntity, NoteResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().content()).isEqualTo("Test note content");
        assertThat(createResponse.getBody().id()).isNotNull();

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<NoteResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes/" + createResponse.getBody().id(),
                HttpMethod.GET, getEntity, NoteResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(getResponse.getBody());
        assertThat(getResponse.getBody().content()).isEqualTo("Test note content");
    }

    @Test
    void findNotesByUserId_ShouldReturnPage() {
        Long userId = createTestUser();

        NoteRequest req1 = new NoteRequest("Note 1");
        NoteRequest req2 = new NoteRequest("Note 2");
        HttpEntity<NoteRequest> entity = new HttpEntity<>(req1, authHeaders());
        restTemplate.exchange(baseUrl() + "/users/" + userId + "/notes", HttpMethod.POST, entity, NoteResponse.class);
        entity = new HttpEntity<>(req2, authHeaders());
        restTemplate.exchange(baseUrl() + "/users/" + userId + "/notes", HttpMethod.POST, entity, NoteResponse.class);

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes?page=0&size=10",
                HttpMethod.GET, getEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Note 1");
        assertThat(response.getBody()).contains("Note 2");
    }

    @Test
    void updateNote_ShouldChangeContent() {
        Long userId = createTestUser();

        NoteRequest createReq = new NoteRequest("Old content");
        HttpEntity<NoteRequest> createEntity = new HttpEntity<>(createReq, authHeaders());
        ResponseEntity<NoteResponse> created = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes", HttpMethod.POST, createEntity, NoteResponse.class);
        Assertions.assertNotNull(created.getBody());
        Long noteId = created.getBody().id();

        NoteRequest updateReq = new NoteRequest("Updated content");
        HttpEntity<NoteRequest> updateEntity = new HttpEntity<>(updateReq, authHeaders());
        ResponseEntity<NoteResponse> updateResponse = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes/" + noteId,
                HttpMethod.PUT, updateEntity, NoteResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(updateResponse.getBody());
        assertThat(updateResponse.getBody().content()).isEqualTo("Updated content");
    }

    @Test
    void deleteNote_ShouldReturn204() {
        Long userId = createTestUser();

        NoteRequest request = new NoteRequest("To delete");
        HttpEntity<NoteRequest> createEntity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<NoteResponse> created = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes", HttpMethod.POST, createEntity, NoteResponse.class);
        Assertions.assertNotNull(created.getBody());
        Long noteId = created.getBody().id();

        HttpEntity<Void> deleteEntity = new HttpEntity<>(authHeaders());
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/users/" + userId + "/notes/" + noteId,
                HttpMethod.DELETE, deleteEntity, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
