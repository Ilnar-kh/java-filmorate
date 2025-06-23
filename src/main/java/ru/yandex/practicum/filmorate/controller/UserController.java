package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрошен список всех пользователей. Всего: {}", users.size());
        return users.values();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.info("Попытка создать пользователя: {}", user);

        // Проверка уникальности email
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Этот email уже используется"
            );
        }

        // Подставляем login как name, если имя не указано
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан с ID = {}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Попытка обновить пользователя: {}", user);

        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Пользователь с ID = " + user.getId() + " не найден"
            );
        }

        // Проверка уникальности email (кроме текущего пользователя)
        if (users.values().stream()
                .anyMatch(u -> !u.getId().equals(user.getId())
                        && u.getEmail().equals(user.getEmail()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Этот email уже используется"
            );
        }

        // Подставляем login как name, если имя не указано
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с ID = {} успешно обновлён", user.getId());
        return user;
    }

    public Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
