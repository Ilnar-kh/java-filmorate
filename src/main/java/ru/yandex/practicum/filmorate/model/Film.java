package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Film {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Set<Long> likes = new HashSet<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @ValidReleaseDate           // ← ваша собственная проверка
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть пустой")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Long duration;

    public int getLikes() {
        log.info("Получено количество лайков: {}", likes.size());
        return likes.size();
    }

    public void addLike(Long userId) {
        log.info("Пользователь с ID = {} поставил лайк фильму", userId);
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        log.info("Пользователь с ID = {} удалил лайк у фильма", userId);
        likes.remove(userId);
    }
}
