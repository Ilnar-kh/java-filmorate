package ru.yandex.practicum.filmorate.storage.film;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
@Data
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        log.info("Создание фильма: {}", film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма с id={}", film.getId());
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Запрошен список всех фильмов. Всего: {}", films.size());
        return films.values();
    }

    @Override
    public Film findById(Long filmId) {
        log.info("Поиск фильма по id={}", filmId);
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        return films.get(filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Пользователь с id={} ставит лайк фильму с id={}", userId, filmId);
        Film film = findById(filmId);
        film.addLike(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.info("Пользователь с id={} удалил лайк фильму с id={}", userId, filmId);
        Film film = findById(filmId);
        film.removeLike(userId);
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
}
