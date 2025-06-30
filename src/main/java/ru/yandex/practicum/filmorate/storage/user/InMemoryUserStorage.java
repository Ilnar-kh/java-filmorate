package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Запрошен список всех пользователей. Всего: {}", users.size());
        return users.values();
    }

    @Override
    public User create(User user) {
        log.info("Попытка создать пользователя: {}", user);

        // Проверка уникальности email
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new ValidationException("Этот email уже используется");
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

    @Override
    public User update(User user) {
        log.info("Попытка обновить пользователя: {}", user);

        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с ID = " + user.getId() + " не найден");
        }

        // Проверка уникальности email (кроме текущего пользователя)
        if (users.values().stream()
                .anyMatch(u -> !u.getId().equals(user.getId())
                        && u.getEmail().equals(user.getEmail()))) {
            throw new ValidationException("Этот email уже используется");
        }

        // Подставляем login как name, если имя не указано
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с ID = {} успешно обновлён", user.getId());
        return user;
    }

    @Override
    public User findById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return users.get(userId);
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
