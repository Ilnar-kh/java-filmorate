package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Попытка создания фильма: {}", film);

        try {
            validateFilm(film);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при создании фильма: {}", e.getMessage());
            throw e;
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан с ID = {}", film.getId());
        return film;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Попытка обновить фильм: {}", newFilm);

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id = {} не найден", newFilm.getId());
            throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        try {
            validateFilm(newFilm);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении фильма: {}", e.getMessage());
            throw e;
        }

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с ID = {} успешно обновлён", newFilm.getId());
        return newFilm;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription() == null || film.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }

        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрошен список всех фильмов. Всего: {}", films.size());
        return films.values();
    }
}
