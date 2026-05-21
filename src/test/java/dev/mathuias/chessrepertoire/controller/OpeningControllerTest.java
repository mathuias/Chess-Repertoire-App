package dev.mathuias.chessrepertoire.controller;

import dev.mathuias.chessrepertoire.auth.JpaUserDetailsService;
import dev.mathuias.chessrepertoire.auth.JwtService;
import dev.mathuias.chessrepertoire.opening.Opening;
import dev.mathuias.chessrepertoire.opening.OpeningRequest;
import dev.mathuias.chessrepertoire.repository.UserRepository;
import dev.mathuias.chessrepertoire.service.OpeningService;
import dev.mathuias.chessrepertoire.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpeningController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "alice@example.com")
class OpeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpeningService openingService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    private final User alice = new User("Alice", "Anderson", "alice@example.com", "hash");

    @BeforeEach
    void resolveCurrentUser() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
    }

    @Test
    void getAll_returnsJsonArrayOfOpenings() throws Exception {
        Opening ruyLopez = new Opening(alice, "Ruy Lopez", "1. e4 e5 2. Nf3 Nc6 3. Bb5", null, null);
        when(openingService.findAll(alice)).thenReturn(List.of(ruyLopez));

        mockMvc.perform(get("/api/openings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Ruy Lopez"))
                .andExpect(jsonPath("$[0].pgn").value("1. e4 e5 2. Nf3 Nc6 3. Bb5"))
                .andExpect(jsonPath("$[0].owner").doesNotExist());
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        when(openingService.findById(99L, alice)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/openings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns201AndCreatedOpening() throws Exception {
        Opening saved = new Opening(alice, "Caro-Kann", "1. e4 c6", null, "solid response to e4");
        when(openingService.create(eq(alice), any(OpeningRequest.class))).thenReturn(saved);

        String body = """
                {"name":"Caro-Kann","pgn":"1. e4 c6","initialFen":null,"notes":"solid response to e4"}
                """;
        mockMvc.perform(post("/api/openings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Caro-Kann"))
                .andExpect(jsonPath("$.pgn").value("1. e4 c6"))
                .andExpect(jsonPath("$.notes").value("solid response to e4"));
    }

    @Test
    void create_rejects400WhenNameIsBlank() throws Exception {
        String body = """
                {"name":"","pgn":"1. e4","initialFen":null,"notes":null}
                """;
        mockMvc.perform(post("/api/openings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_returns204WhenSuccessful() throws Exception {
        when(openingService.delete(42L, alice)).thenReturn(true);

        mockMvc.perform(delete("/api/openings/42"))
                .andExpect(status().isNoContent());
        verify(openingService).delete(42L, alice);
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        when(openingService.delete(42L, alice)).thenReturn(false);

        mockMvc.perform(delete("/api/openings/42"))
                .andExpect(status().isNotFound());
    }
}
