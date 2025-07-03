package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    private Map<Long, FriendshipStatus> friendshipStatuses = new HashMap<>();

    private Set<Long> friends = new HashSet<>();

    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой и должна содержать символ '@'")
    @Pattern(
            regexp = "^.+@.+$",
            message = "Электронная почта не может быть пустой и должна содержать символ '@'"
    )
    private String email;

    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    @Pattern(
            regexp = "\\S+",
            message = "Логин не может быть пустым и содержать пробелы"
    )
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public Set<Long> getFriends() {
        log.info("Получен список друзей пользователя с ID = {}", id);
        return friends;
    }

    public void addFriend(Long friendId) {
        log.info("Пользователь с ID = {} добавил друга с ID = {}", id, friendId);
        // помечаем в статусах как CONFIRMED
        friendshipStatuses.put(friendId, FriendshipStatus.CONFIRMED);
        // и в списке ID
        friends.add(friendId);
    }

    public void removeFriend(Long friendId) {
        log.info("Пользователь с ID = {} удалил друга с ID = {}", id, friendId);
        friendshipStatuses.remove(friendId);
        friends.remove(friendId);
    }

    public User(Long id,
                String email,
                String login,
                String name,
                LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public void setFriends(Set<Long> friends) {
        this.friends = friends;
    }

    @JsonSetter("friends")
    public void setFriendsFromJson(Object raw) {
        if (raw instanceof Collection<?>) {
            Set<Long> set = new HashSet<>();
            for (Object o : (Collection<?>) raw) {
                if (o instanceof Number) {
                    set.add(((Number) o).longValue());
                }
            }
            this.friends = set;
        } else {
            // пришло число, null или ещё что-то — просто оставляем пустое множество
            this.friends = new HashSet<>();
        }
    }
}
