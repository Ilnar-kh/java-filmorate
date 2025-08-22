package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Film {

    private Set<Long> likes = new LinkedHashSet<>();

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @ValidReleaseDate
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть пустой")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Long duration;

    private Set<Genre> genres = new LinkedHashSet<>();

    @NotNull(message = "Рейтинг MPA не может быть пустым")
    private MpaRating mpa;

    private List<Director> directors = new ArrayList<>();

    public int getLikes() {
        int size = likes != null ? likes.size() : 0;
        log.info("Получено количество лайков: {}", size);
        return size;
    }

    public void addLike(Long userId) {
        log.info("Пользователь с ID = {} поставил лайк фильму", userId);
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        log.info("Пользователь с ID = {} удалил лайк у фильма", userId);
        likes.remove(userId);
    }

    @JsonSetter("likes")
    public void setLikes(Set<Long> likes) {
        this.likes = likes;
    }

    public Film(Long id,
                String name,
                String description,
                LocalDate releaseDate,
                long duration) {
        this.likes = new LinkedHashSet<>();
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = new LinkedHashSet<>();
        this.directors = new ArrayList<>();
    }
}
