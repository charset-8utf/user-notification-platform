package com.crud.controller;

import com.crud.config.security.SecurityConfig;
import com.crud.config.WebMvcTestSecuritySupport;
import tools.jackson.databind.json.JsonMapper;
import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.exception.NoteNotFoundException;
import com.crud.service.NoteService;
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

@WebMvcTest(NoteController.class)
@Import({SecurityConfig.class, WebMvcTestSecuritySupport.class})
@WithMockUser
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    private final JsonMapper jsonMapper = JsonMapper.shared();

    @Test
    void createNote_ShouldReturn201() throws Exception {
        NoteRequest request = new NoteRequest("Test note");
        NoteResponse response = new NoteResponse(1L, "Test note", LocalDateTime.now(), LocalDateTime.now());

        when(noteService.createNote(anyLong(), any(NoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Test note"));
    }

    @Test
    void findNoteById_ShouldReturn200() throws Exception {
        NoteResponse response = new NoteResponse(1L, "Test note", LocalDateTime.now(), LocalDateTime.now());

        when(noteService.findNoteById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test note"));
    }

    @Test
    void findNoteById_WhenNotFound_ShouldReturn404() throws Exception {
        when(noteService.findNoteById(1L, 999L)).thenThrow(new NoteNotFoundException(999L));

        mockMvc.perform(get("/api/users/1/notes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findNotesByUserId_ShouldReturn200() throws Exception {
        NoteResponse response = new NoteResponse(1L, "Note 1", LocalDateTime.now(), LocalDateTime.now());

        when(noteService.findNotesByUserId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/users/1/notes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateNote_ShouldReturn200() throws Exception {
        NoteRequest request = new NoteRequest("Updated note");
        NoteResponse response = new NoteResponse(1L, "Updated note", LocalDateTime.now(), LocalDateTime.now());

        when(noteService.updateNote(1L, 1L, request)).thenReturn(response);

        mockMvc.perform(put("/api/users/1/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated note"));
    }

    @Test
    void deleteNote_ShouldReturn204() throws Exception {
        doNothing().when(noteService).deleteNote(1L, 1L);

        mockMvc.perform(delete("/api/users/1/notes/1"))
                .andExpect(status().isNoContent());
    }
}
