package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final DirectorService directorService;
    private final FeedService feedService;

    @Autowired
    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            DirectorStorage directorStorage,
            DirectorService directorService,
            FeedService feedService
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.directorService = directorService;
        this.feedService = feedService;
    }

    public Film create(Film film) {
        if (film.getMpa() == null || !mpaStorage.existsById(film.getMpa().getId())) {
            throw new NotFoundException("MPA rating with id " +
                    (film.getMpa() != null ? film.getMpa().getId() : "null") + " does not exist.");
        }

        normalizeGenres(film);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Integer> genreIds = film.getGenres().stream().map(Genre::getId).toList();
            Set<Integer> existingIds = genreStorage.findExistingIds(genreIds);
            for (Integer id : genreIds) {
                if (!existingIds.contains(id)) {
                    throw new NotFoundException("Жанр с id " + id + " не существует.");
                }
            }
        }

        Film saved = filmStorage.create(film);
        // сохраняем жанры ИМЕННО из запроса
        film.setId(saved.getId());
        filmStorage.saveFilmGenres(film);

        // режиссёры — тоже из запроса
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            updateFilmDirectors(film);
        }

        return findById(saved.getId());
    }

    public Film update(Film film) {
        getFilmOrThrow(film.getId());
        normalizeGenres(film);
        filmStorage.update(film);
        filmStorage.saveFilmGenres(film);
        filmStorage.removeFilmDirectors(film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            updateFilmDirectors(film);
        }
        return findById(film.getId());
    }


    private void updateFilmDirectors(Film film) {
        for (Director director : film.getDirectors()) {
            if (director.getId() != null) {
                directorService.getDirectorById(director.getId());
                filmStorage.addFilmDirector(film.getId(), director.getId());
            }
        }
    }

    public Collection<Film> findAll() {
        List<Film> films = filmStorage.findAll();
        for (Film film : films) {
            film.setDirectors(directorStorage.getFilmDirectors(film.getId().intValue()));
        }
        return films;
    }

    public Film findById(Long filmId) {
        Film film = getFilmOrThrow(filmId);
        film.setDirectors(directorStorage.getFilmDirectors(filmId.intValue()));
        return film;
    }

    public void putLike(Long filmId, Long userId) {
        getUserOrThrow(userId);
        getFilmOrThrow(filmId);
        try {
            filmStorage.addLike(filmId, userId);
        } catch (DataAccessException ignored) {
        }
        feedService.likeFromUser(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        getUserOrThrow(userId);
        getFilmOrThrow(filmId);
        filmStorage.removeLike(filmId, userId);
        feedService.unlikeFromUser(filmId, userId);
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        if (count <= 0) return java.util.Collections.emptyList();
        List<Film> films = filmStorage.getPopularFilms(count, genreId, year);
        for (Film film : films) {
            film.setDirectors(directorStorage.getFilmDirectors(film.getId().intValue()));
        }
        return films;
    }

    public List<Film> getFilmsByDirectorSorted(Long directorId, String sortBy) {
        // 404 если нет
        directorService.getDirectorById(directorId);

        List<Film> films;
        if (sortBy.equals("year")) {
            films = filmStorage.getFilmsByDirectorSortedByYear(directorId);
        } else if (sortBy.equals("likes")) {
            films = filmStorage.getFilmsByDirectorSortedByLikes(directorId);
        } else {
            throw new IllegalArgumentException("Недопустимый параметр сортировки: " + sortBy);
        }

        for (Film film : films) {
            film.setDirectors(directorStorage.getFilmDirectors(film.getId().intValue()));
        }
        return films;
    }

    public int removeById(Long filmId) {
        return filmStorage.removeById(filmId);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        log.debug("Service: getCommonFilms userId={}, friendId={}", userId, friendId);
        List<Film> films = filmStorage.findCommonFilms(userId, friendId);
        for (Film film : films) {
            film.setDirectors(directorStorage.getFilmDirectors(film.getId().intValue()));
        }
        return films;
    }

    public List<Film> searchFilms(String query, String by) {
        List<Film> films = switch (by) {
            case "title" -> filmStorage.searchByTitle(query);
            case "director" -> filmStorage.searchByDirector(query);
            case "title,director", "director,title" -> filmStorage.searchByTitleAndDirector(query);
            default -> throw new IllegalArgumentException(
                    "Parameter 'by' must be 'title', 'director', or both separated by comma."
            );
        };
        for (Film film : films) {
            film.setDirectors(directorStorage.getFilmDirectors(film.getId().intValue()));
        }
        return films;
    }

    private Film getFilmOrThrow(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film " + id + " not found"));
    }

    private User getUserOrThrow(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    private void normalizeGenres(Film film) {
        if (film.getGenres() != null) {
            film.setGenres(new LinkedHashSet<>(film.getGenres())); // без дублей, сохраняем порядок
        }
    }
}
