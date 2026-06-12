package com.crud.controller;

import com.crud.config.security.SecurityConfig;
import com.crud.config.WebMvcTestSecuritySupport;
import tools.jackson.databind.json.JsonMapper;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.exception.ProfileNotFoundException;
import com.crud.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, WebMvcTestSecuritySupport.class})
@WithMockUser
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    private final JsonMapper jsonMapper = JsonMapper.shared();

    @Test
    void createProfile_ShouldReturn201() throws Exception {
        ProfileRequest request = new ProfileRequest("+1234567890", "Test Address");
        ProfileResponse response = new ProfileResponse(1L, 1L, "+1234567890", "Test Address", LocalDateTime.now(), LocalDateTime.now());

        when(profileService.createProfile(anyLong(), any(ProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/profiles/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("+1234567890"));
    }

    @Test
    void findProfileByUserId_ShouldReturn200() throws Exception {
        ProfileResponse response = new ProfileResponse(1L, 1L, "+1234567890", "Test Address", LocalDateTime.now(), LocalDateTime.now());

        when(profileService.findProfileByUserId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/profiles/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+1234567890"));
    }

    @Test
    void findProfileByUserId_WhenNotFound_ShouldReturn404() throws Exception {
        when(profileService.findProfileByUserId(999L)).thenThrow(new ProfileNotFoundException(999L));

        mockMvc.perform(get("/api/profiles/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllProfiles_ShouldReturn200() throws Exception {
        ProfileResponse response = new ProfileResponse(1L, 1L, "+1234567890", "Address", LocalDateTime.now(), LocalDateTime.now());

        when(profileService.findAllProfiles(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/profiles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateProfile_ShouldReturn200() throws Exception {
        ProfileRequest request = new ProfileRequest("+999", "Updated Address");
        ProfileResponse response = new ProfileResponse(1L, 1L, "+999", "Updated Address", LocalDateTime.now(), LocalDateTime.now());

        when(profileService.updateProfile(anyLong(), any(ProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/profiles/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+999"));
    }

    @Test
    void deleteProfile_ShouldReturn204() throws Exception {
        doNothing().when(profileService).deleteProfile(1L);

        mockMvc.perform(delete("/api/profiles/user/1"))
                .andExpect(status().isNoContent());
    }
}
