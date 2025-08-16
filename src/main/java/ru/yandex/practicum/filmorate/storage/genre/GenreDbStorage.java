package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;

    public GenreDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Genre mapRowToGenre(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("id"));
        genre.setName(resultSet.getString("name"));
        return genre;
    }

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbc.query(sql, this::mapRowToGenre);
    }

    @Override
    public Genre findById(Integer id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";

        try {
            return jdbc.queryForObject(sql, this::mapRowToGenre, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }

    @Override
    public Set<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return new HashSet<>(jdbc.query(sql, this::mapRowToGenre, filmId));
    }

    public Set<Integer> findExistingIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id FROM genres WHERE id IN (" + placeholders + ")";
        List<Integer> foundIds = jdbc.queryForList(sql, Integer.class, ids.toArray());
        return new HashSet<>(foundIds);

    }
}

