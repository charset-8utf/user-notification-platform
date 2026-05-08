package com.crud.controller;

import com.crud.config.SecurityConfig;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@WithMockUser
class UserValidationEdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final JsonMapper jsonMapper = JsonMapper.shared();

    @BeforeEach
    void stubCreateUser() {
        when(userService.createUser(any(UserRequest.class)))
                .thenAnswer(invocation -> {
                    UserRequest req = invocation.getArgument(0);
                    return new UserResponse(1L, req.name(), req.email(), req.age(), LocalDateTime.now());
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid@email.com", "user.name+tag@example.co.uk", "a@b.co"})
    void createUser_WithValidEmail_ShouldReturn201(String email) throws Exception {
        UserRequest request = new UserRequest("John", email, 25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "invalid", "@example.com", "user@", "user@.com"})
    void createUser_WithInvalidEmail_ShouldReturn400(String email) throws Exception {
        UserRequest request = new UserRequest("John", email, 25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 201",
            "18, 201",
            "100, 201",
            "150, 201"
    })
    void createUser_WithValidAges_ShouldReturn201(int age) throws Exception {
        UserRequest request = new UserRequest("John", "test@example.com", age);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 400",
            "151, 400",
            "999, 400"
    })
    void createUser_WithInvalidAges_ShouldReturn400(int age) throws Exception {
        UserRequest request = new UserRequest("John", "test@example.com", age);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void createUser_WithBlankName_ShouldReturn400(String name) throws Exception {
        UserRequest request = new UserRequest(name, "test@example.com", 25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "John Doe", "Иван Иванов", "Patrick", "Jean Pierre"})
    void createUser_WithValidName_ShouldReturn201(String name) throws Exception {
        UserRequest request = new UserRequest(name, "test@example.com", 25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
