package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    private final Set<Long> friends = new HashSet<>();

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
        friends.add(friendId);
    }

    public void removeFriend(Long friendId) {
        log.info("Пользователь с ID = {} удалил друга с ID = {}", id, friendId);
        friends.remove(friendId);
    }
}
