package dev.mathuias.springdemo.controller;

import dev.mathuias.springdemo.service.UserService;
import dev.mathuias.springdemo.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getAll_returnsJsonArrayFromService() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                new User("Alice", "Anderson"),
                new User("Bob", "Brown")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstname").value("Alice"))
                .andExpect(jsonPath("$[0].lastname").value("Anderson"))
                .andExpect(jsonPath("$[1].firstname").value("Bob"))
                .andExpect(jsonPath("$[1].lastname").value("Brown"));
    }

    @Test
    void getById_returns200AndUserWhenFound() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(new User("Alice", "Anderson")));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Alice"))
                .andExpect(jsonPath("$.lastname").value("Anderson"));
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
