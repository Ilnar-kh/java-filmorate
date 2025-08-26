package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Set;

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
        log.info("POST  /films — создание фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("PUT   /films — обновление фильма: {}", film);
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("GET   /films — запрос всех фильмов");
        return (List<Film>) filmService.findAll();
    }

    @GetMapping("/{filmId}")
    public Film findById(
            @PathVariable Long filmId
    ) {
        log.info("GET   /films/{} — запрос фильма по id", filmId);
        return filmService.findById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void putLike(
            @PathVariable Long filmId,
            @PathVariable Long userId
    ) {
        log.info("PUT   /films/{}/like/{} — пользователь ставит лайк", filmId, userId);
        filmService.putLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(
            @PathVariable Long filmId,
            @PathVariable Long userId
    ) {
        log.info("DELETE /films/{}/like/{} — пользователь удаляет лайк", filmId, userId);
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(name = "count", defaultValue = "10")
             int count,
            @RequestParam(name = "genreId", required = false) Integer genreId,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("GET /films/popular?count={}&genreId={}&year={} — запрос популярных фильмов",
                count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("GET   /films/common?userId={}&friendId={} — запрос общих фильмов", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirectorSorted(
            @PathVariable Long directorId,
            @RequestParam(name = "sortBy", defaultValue = "year") String sortBy
    ) {
        log.info("GET /films/director/{}?sortBy={} — получение фильмов режиссера с сортировкой", directorId, sortBy);
        return filmService.getFilmsByDirectorSorted(directorId, sortBy);
    }

    @DeleteMapping("/{id}")
    public void removeById(@PathVariable
                           Long id) {
        if (filmService.removeById(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм с id=" + id + " не найден");
        }
        log.info("DELETE /films/{} - удаление фильма по id", id);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query,
                                  @RequestParam String by) {
        String byFields = by.trim().toLowerCase();
        log.info("GET /films/search?query={}&by={}", query, byFields);

        Set<String> allowed = Set.of("title", "director", "title,director", "director,title");

        if (!allowed.contains(byFields)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Parameter 'by' must be 'title', 'director', or 'title,director'"
            );
        }

        return filmService.searchFilms(query.trim(), byFields);
    }
}
