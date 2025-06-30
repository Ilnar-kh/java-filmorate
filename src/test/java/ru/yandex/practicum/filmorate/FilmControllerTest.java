package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    // Успешное создание
    @Test
    void createValidFilm() throws Exception {
        Film film = new Film(null, "Title", "Description", LocalDate.of(2000, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Title"));
    }

    // Название не может быть пустым
    @Test
    void shouldThrowIfNameIsBlank() throws Exception {
        Film film = new Film(null, " ", "Description", LocalDate.of(2000, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название не может быть пустым"));
    }

    // Описание слишком длинное
    @Test
    void shouldThrowIfDescriptionTooLong() throws Exception {
        String longDesc = "a".repeat(201);
        Film film = new Film(null, "Valid", longDesc, LocalDate.of(2000, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description")
                        .value("Максимальная длина описания - 200 символов"));
    }

    // Дата релиза слишком старая
    @Test
    void shouldThrowIfReleaseDateTooOld() throws Exception {
        Film film = new Film(null, "Valid", "Desc", LocalDate.of(1800, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.releaseDate")
                        .value("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    // Длительность неположительная
    @Test
    void shouldThrowIfDurationNonPositive() throws Exception {
        Film film = new Film(null, "Valid", "Desc", LocalDate.of(2000, 1, 1), 0L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration")
                        .value("Продолжительность фильма должна быть положительным числом"));
    }
}
