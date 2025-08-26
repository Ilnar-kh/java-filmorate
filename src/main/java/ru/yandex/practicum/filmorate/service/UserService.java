package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FeedService feedService;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FeedService feedService) {
        this.userStorage = userStorage;
        this.feedService = feedService;
    }

    public User create(User user) {
        postProcessName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        postProcessName(user);
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        log.info("Запрошен список всех пользователей");
        return userStorage.findAll();
    }

    public User findById(Long id) {
        log.info("Поиск пользователя по id={}", id);
        return userStorage.findById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Пользователь {} добавляет в друзья пользователя {}", userId, friendId);
        userStorage.addFriend(userId, friendId);
        feedService.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Пользователь {} удаляет из друзей пользователя {}", userId, friendId);
        userStorage.removeFriend(userId, friendId);
        feedService.deleteFriend(userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        log.info("Запрошен список друзей пользователя {}", userId);
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Запрошен список общих друзей пользователей {} и {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public int removeById(Long userId) {
        log.info("Запрос на удаление пользователя {}", userId);
        return userStorage.removeById(userId);
    }

    private void postProcessName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}