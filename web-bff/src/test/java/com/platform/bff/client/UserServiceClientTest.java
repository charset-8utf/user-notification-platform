package com.platform.bff.client;

import com.platform.bff.dto.ProfileSummary;
import com.platform.bff.dto.UserSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserServiceClientTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final UserServiceClient client = new UserServiceClient(builder.build());

    @AfterEach
    void verifyServer() {
        server.verify();
    }

    @Test
    void getCurrentUser_returnsUserSummary() {
        server.expect(requestTo("/api/users/me"))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "name": "admin",
                          "email": "admin@example.com",
                          "age": 30
                        }
                        """, MediaType.APPLICATION_JSON));

        UserSummary user = client.getCurrentUser("Bearer token");

        assertThat(user).isNotNull();
        assertThat(user.name()).isEqualTo("admin");
        assertThat(user.email()).isEqualTo("admin@example.com");
    }

    @Test
    void getProfileByUserId_returnsProfileSummary() {
        server.expect(requestTo("/api/profiles/user/1"))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("""
                        {
                          "id": 10,
                          "phone": "+7999",
                          "address": "Moscow"
                        }
                        """, MediaType.APPLICATION_JSON));

        ProfileSummary profile = client.getProfileByUserId(1L, "Bearer token");

        assertThat(profile).isNotNull();
        assertThat(profile.address()).isEqualTo("Moscow");
    }
}
