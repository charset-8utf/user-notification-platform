package com.platform.bff.controller;

import com.platform.bff.dto.MeResponse;
import com.platform.bff.dto.NotificationSummary;
import com.platform.bff.dto.ProfileSummary;
import com.platform.bff.dto.UserSummary;
import com.platform.bff.facade.MeFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MeControllerWebMvcTest {

    @Mock
    private MeFacade meFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MeController(meFacade)).build();
    }

    @Test
    void me_returnsAggregatedPayload() throws Exception {
        MeResponse response = new MeResponse(
                new UserSummary(1L, "admin", "admin@example.com", 30),
                new ProfileSummary(10L, "+7999", "Moscow"),
                new NotificationSummary("email", "DELIVERED", "Welcome"));
        when(meFacade.loadCurrentUser(eq("Bearer token"))).thenReturn(response);

        mockMvc.perform(get("/bff/me").header(AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("admin"))
                .andExpect(jsonPath("$.profile.address").value("Moscow"))
                .andExpect(jsonPath("$.lastNotification.status").value("DELIVERED"));
    }
}
