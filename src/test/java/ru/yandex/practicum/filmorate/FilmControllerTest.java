package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setup() {
        controller = new FilmController();
    }

    @Test
    void createValidFilm() {
        Film film = new Film(null, "Title", "Description", LocalDate.of(2000, 1, 1), 120L);
        Film created = controller.create(film);

        assertNotNull(created.getId());
        assertEquals("Title", created.getName());
    }

    @Test
    void shouldThrowIfNameIsBlank() {
        Film film = new Film(null, " ", "Description", LocalDate.of(2000, 1, 1), 120L);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Название"));
    }

    @Test
    void shouldThrowIfDescriptionTooLong() {
        String longDesc = "a".repeat(201);
        Film film = new Film(null, "Valid", longDesc, LocalDate.of(2000, 1, 1), 120L);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("длина описания"));
    }

    @Test
    void shouldThrowIfReleaseDateTooOld() {
        Film film = new Film(null, "Valid", "Desc", LocalDate.of(1800, 1, 1), 120L);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Дата релиза"));
    }

    @Test
    void shouldThrowIfDurationNonPositive() {
        Film film = new Film(null, "Valid", "Desc", LocalDate.of(2000, 1, 1), 0L);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Продолжительность"));
    }
}
