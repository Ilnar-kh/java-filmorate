package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createValidUser() throws Exception {
        User user = new User(null, "user@example.com", "login", "User", LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSetLoginAsNameIfNameIsBlank() throws Exception {
        User user = new User(null, "user@example.com", "login", "", LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("login"));
    }

    @Test
    void shouldReturnErrorForInvalidEmail() throws Exception {
        User user = new User(null, "invalid-email", "login", "Name", LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Электронная почта не может быть пустой и должна содержать символ '@'"));
    }

    @Test
    void shouldReturnErrorForBlankLogin() throws Exception {
        User user = new User(null, "user@example.com", " ", "Name", LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnErrorForFutureBirthday() throws Exception {
        User user = new User(null, "user@example.com", "login", "Name", LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthday").value("Дата рождения не может быть в будущем"));
    }

    @Test
    void shouldReturnErrorForDuplicateEmail() throws Exception {
        User user1 = new User(null, "dup@example.com", "login1", "Name1", LocalDate.of(2000, 1, 1));
        User user2 = new User(null, "dup@example.com", "login2", "Name2", LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Этот email уже используется"));
    }
}
