package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FeedService feedService;
    private final RecommendationService recommendationService;

    public UserController(UserService userService, FeedService feedService, RecommendationService recommendationService) {
        this.userService = userService;
        this.feedService = feedService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users — получение всех пользователей");
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users — создание пользователя: {}", user);
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("PUT /users — обновление пользователя с ID = {}", user.getId());
        if (userService.findById(user.getId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable Long id,
            @PathVariable Long friendId) {
        log.info("PUT /users/{}/friends/{} — добавление друга", id, friendId);
        checkUsersExist(id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(
            @PathVariable Long id,
            @PathVariable Long friendId) {
        log.info("DELETE /users/{}/friends/{} — удаление из друзей", id, friendId);
        checkUsersExist(id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("GET /users/{}/friends — список друзей", id);
        if (userService.findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        return (List<User>) userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("GET /users/{}/friends/common/{} — список общих друзей", id, otherId);
        checkUsersExist(id, otherId);
        return (List<User>) userService.getCommonFriends(id, otherId);
    }

    @DeleteMapping("/{id}")
    public void removeById(@PathVariable
                           Long id) {
        if (userService.removeById(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с id=" + id + " не найден");
        }
        log.info("DELETE /users/{} - удаление пользователя по id", id);
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        log.info("GET /users/{} - получение пользователя по id", id);
        return userService.findById(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) {
        return recommendationService.getRecommendations(id);
    }

    private void checkUsersExist(Long id, Long otherId) {
        if (userService.findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с id=" + id + " не найден");
        }
        if (userService.findById(otherId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с id=" + otherId + " не найден");
        }
    }

    @GetMapping("/{id}/feed")
    public Collection<Feed> getUserFeed(@PathVariable Long id) {
        log.info("Запрос ленты событий для пользователя с ID: {}", id);
        if (userService.findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с id=" + id + " не найден");
        }
        return feedService.feeds(id);
    }
}
