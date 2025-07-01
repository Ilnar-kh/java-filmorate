package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films — создание фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("PUT /films — обновление фильма: {}", film);
        return filmService.update(film);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films — запрос всех фильмов");
        return filmService.findAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable @Positive(message = "id фильма должно быть положительным") Long id,
                        @PathVariable @Positive(message = "id пользователя должно быть положительным") Long userId) {
        log.info("PUT /films/{}/like/{} — пользователь ставит лайк", id, userId);
        filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(
            @PathVariable @Positive(message = "ID фильма должно быть положительным") Long id,
            @PathVariable @Positive(message = "ID пользователя должно быть положительным") Long userId) {
        log.info("DELETE /films/{}/like/{} — пользователь удаляет лайк", id, userId);
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("GET /films/popular?count={} — запрос популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }
}
