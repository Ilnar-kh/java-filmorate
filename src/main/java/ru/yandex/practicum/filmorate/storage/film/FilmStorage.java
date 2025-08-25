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

    List<Film> getFilmsByDirectorSortedByYear(Long directorId);

    List<Film> getFilmsByDirectorSortedByLikes(Long directorId);

    void addFilmDirector(Long filmId, Long directorId);

    void removeFilmDirectors(Long filmId);

    int removeById(Long filmId);

    List<Film> findCommonFilms(Long userId, Long friendId);

    List<Film> searchByTitle(String query);

    List<Film> searchByDirector(String query);

    List<Film> searchByTitleAndDirector(String query);
}
