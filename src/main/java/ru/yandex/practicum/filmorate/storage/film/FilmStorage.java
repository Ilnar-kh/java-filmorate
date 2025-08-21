package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> findAll();

    Film findById(Long filmId);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    void saveFilmGenres(Film film);

    List<Film> getPopularFilms(int count);

    int removeById(Long filmId);

    List<Film> findCommonFilms(Long userId, Long friendId);
}
