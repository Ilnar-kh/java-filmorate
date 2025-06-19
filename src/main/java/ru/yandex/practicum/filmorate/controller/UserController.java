package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрошен список всех пользователей. Всего: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Попытка создать пользователя: {}", user);

        try {
            validateUser(user);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан с ID = {}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Попытка обновить пользователя: {}", newUser);

        if (newUser.getId() == null) {
            log.warn("Обновление отклонено: ID не указан");
            throw new ValidationException("ID должен быть указан");
        }

        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Обновление отклонено: пользователь с ID = {} не найден", newUser.getId());
            throw new ValidationException("Пользователь с таким ID не найден");
        }

        try {
            validateUser(newUser, true); // режим обновления
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении пользователя: {}", e.getMessage());
            throw e;
        }

        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        log.info("Пользователь с ID = {} успешно обновлён", newUser.getId());
        return oldUser;
    }

    private void validateUser(User user) {
        validateUser(user, false);
    }

    private void validateUser(User user, boolean isUpdate) {
        // Email
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ '@'");
        }
        for (User existing : users.values()) {
            if (!isUpdate || !existing.getId().equals(user.getId())) {
                if (user.getEmail().equals(existing.getEmail())) {
                    throw new ValidationException("Этот email уже используется");
                }
            }
        }

        // Login
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        // Имя по умолчанию
        if (!isUpdate && (user.getName() == null || user.getName().isBlank())) {
            user.setName(user.getLogin());
        }

        // Дата рождения
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    public Long getNextId() {
        return idCounter++;
    }
}
