package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Director getDirectorById(Long id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(Long id);

    List<Director> getFilmDirectors(int filmId);

    void addFilmDirector(int filmId, int directorId);

    void removeFilmDirectors(int filmId);

    Map<Long, List<Director>> getDirectorsByFilmIds(Set<Long> filmIds);
}
