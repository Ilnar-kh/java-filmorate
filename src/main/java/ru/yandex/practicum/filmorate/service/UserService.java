package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Пользователь {} добавляет в друзья пользователя {}", userId, friendId);
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        user.addFriend(friendId);
        friend.addFriend(userId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(userId);
    }

    public List<User> getFriends(Long id) {
        return userStorage.findById(id).getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }


    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherFriends = other.getFriends();

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
