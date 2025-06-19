package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setup() {
        controller = new UserController();
    }

    @Test
    void createValidUser() {
        User user = new User(null, "user@example.com", "login", "User", LocalDate.of(2000, 1, 1));
        User created = controller.create(user);

        assertNotNull(created.getId());
        assertEquals("user@example.com", created.getEmail());
        assertEquals("login", created.getLogin());
    }

    @Test
    void shouldSetLoginAsNameIfNameIsBlank() {
        User user = new User(null, "user@example.com", "login", "", LocalDate.of(2000, 1, 1));
        User created = controller.create(user);

        assertEquals("login", created.getName());
    }

    @Test
    void shouldThrowIfEmailIsInvalid() {
        User user = new User(null, "invalid-email", "login", "Name", LocalDate.of(2000, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(ex.getMessage().contains("Электронная почта"));
    }

    @Test
    void shouldThrowIfLoginIsBlank() {
        User user = new User(null, "user@example.com", " ", "Name", LocalDate.of(2000, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(ex.getMessage().contains("Логин"));
    }

    @Test
    void shouldThrowIfBirthdayInFuture() {
        User user = new User(null, "user@example.com", "login", "Name", LocalDate.now().plusDays(1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(ex.getMessage().contains("Дата рождения"));
    }

    @Test
    void shouldThrowIfDuplicateEmail() {
        User user1 = new User(null, "dup@example.com", "login1", "Name1", LocalDate.of(2000, 1, 1));
        User user2 = new User(null, "dup@example.com", "login2", "Name2", LocalDate.of(1990, 1, 1));

        controller.create(user1);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user2));
        assertTrue(ex.getMessage().contains("уже используется"));
    }
}
