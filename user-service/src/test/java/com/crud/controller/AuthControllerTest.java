package com.crud.controller;

import com.crud.config.security.JwtConfig;
import com.crud.config.security.JwtSecurityConfig;
import com.crud.config.security.SecurityBeansConfig;
import com.crud.config.WebMvcJwtTestSupport;
import com.crud.config.WebMvcTestSecuritySupport;
import com.crud.dto.auth.LoginRequest;
import com.crud.dto.auth.TokenResponse;
import com.crud.exception.GlobalExceptionHandler;
import com.crud.security.AuthService;
import com.platform.commons.observability.ExceptionMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityBeansConfig.class, JwtConfig.class, JwtSecurityConfig.class,
        WebMvcTestSecuritySupport.class, WebMvcJwtTestSupport.class})
@ActiveProfiles({"test", "jwt"})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ExceptionMetrics exceptionMetrics;

    @MockitoBean
    private com.crud.security.jwt.RefreshTokenStore refreshTokenStore;

    @Test
    void login_returnsTokens() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("access", "refresh", "Bearer", 900));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
