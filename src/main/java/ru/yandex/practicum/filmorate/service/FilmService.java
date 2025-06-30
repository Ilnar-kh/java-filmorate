package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void putLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        film.addLike(userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        film.removeLike(userId);
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Запрошен список популярных фильмов, count={}", count);
        if (count <= 0) {
            return Collections.emptyList();
        }
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingLong(Film::getLikes).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
