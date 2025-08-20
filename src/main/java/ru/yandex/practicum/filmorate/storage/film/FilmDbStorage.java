package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        String insertQuery = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setLong(4, film.getDuration());
            preparedStatement.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return preparedStatement;
        }, generatedKeyHolder);
        film.setId(generatedKeyHolder.getKey().longValue());
        return film;
    }

    @Override
    public Film update(Film film) {
        String updateQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(updateQuery,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return findById(film.getId());
    }

    @Override
    public List<Film> findAll() {
        String selectAllQuery = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON m.id = f.mpa_id
                """;
        return jdbcTemplate.query(selectAllQuery, this::mapRowToFilm);
    }

    @Override
    public Film findById(Long filmId) {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON m.id = f.mpa_id
                WHERE f.id = ?
                """;
        Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, filmId);

        Map<Long, Set<Genre>> genresByFilm =
                loadGenresByFilmIds(java.util.Set.of(film.getId()));

        film.setGenres(new java.util.HashSet<>(
                genresByFilm.getOrDefault(film.getId(),
                        java.util.Collections.emptySet())
        ));
        return film;
    }


    @Override
    public void addLike(Long filmId, Long userId) {
        String insertLikeQuery = "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertLikeQuery, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String deleteLikeQuery = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(deleteLikeQuery, filmId, userId);
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNumber) throws SQLException {

        MpaRating mpa = null;
        int mpaId = resultSet.getInt("mpa_id");
        if (!resultSet.wasNull()) {
            mpa = new MpaRating();
            mpa.setId(mpaId);
            mpa.setName(resultSet.getString("mpa_name"));
        }

        Film film = Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getLong("duration"))
                .mpa(mpa)
                .build();

        return film;
    }

    public void saveFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        List<Integer> genreIds = film.getGenres().stream()
                .filter(genre -> genre != null && genre.getId() != null)
                .map(Genre::getId)
                .distinct()
                .toList();

        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(
                insertSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setLong(1, film.getId());
                        pstmt.setInt(2, genreIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return genreIds.size();
                    }
                }
        );
    }

    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, COUNT(fl.user_id) AS likes
                FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                ORDER BY likes DESC
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> Film.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getLong("duration"))
                        .mpa(new MpaRating(rs.getInt("mpa_id")))
                        .build(),
                count
        );

        if (films.isEmpty()) return films;

        // одним запросом тянем жанры для всех фильмов
        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(java.util.stream.Collectors.toSet());

        Map<Long, Set<Genre>> genresByFilm = loadGenresByFilmIds(filmIds);

        // расставляем жанры без дополнительных запросов
        for (Film film : films) {
            film.setGenres(genresByFilm.getOrDefault(
                    film.getId(),
                    java.util.Collections.emptySet()
            ));
        }

        return films;
    }

    private Map<Long, Set<Genre>> loadGenresByFilmIds(Set<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(java.util.stream.Collectors.joining(","));

        String sql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id IN (""" + placeholders + ")";

        Map<Long, Set<Genre>> result = new java.util.HashMap<>();

        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            result.computeIfAbsent(filmId, k -> new java.util.HashSet<>()).add(genre);
        });

        return result;
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByYear(Long directorId) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name
                FROM films f
                JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE fd.director_id = ?
                ORDER BY f.release_date
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);

        if (!films.isEmpty()) {
            // Загрузка жанров для всех фильмов
            Set<Long> filmIds = films.stream()
                    .map(Film::getId)
                    .collect(java.util.stream.Collectors.toSet());

            Map<Long, Set<Genre>> genresByFilm = loadGenresByFilmIds(filmIds);

            for (Film film : films) {
                film.setGenres(genresByFilm.getOrDefault(
                        film.getId(),
                        java.util.Collections.emptySet()
                ));
            }
        }

        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikes(Long directorId) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, COUNT(fl.user_id) AS likes
                FROM films f
                JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                WHERE fd.director_id = ?
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                ORDER BY likes DESC
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);

        if (!films.isEmpty()) {
            // Загрузка жанров для всех фильмов
            Set<Long> filmIds = films.stream()
                    .map(Film::getId)
                    .collect(java.util.stream.Collectors.toSet());

            Map<Long, Set<Genre>> genresByFilm = loadGenresByFilmIds(filmIds);

            for (Film film : films) {
                film.setGenres(genresByFilm.getOrDefault(
                        film.getId(),
                        java.util.Collections.emptySet()
                ));
            }
        }

        return films;
    }

    @Override
    public void addFilmDirector(Long filmId, Long directorId) {
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, directorId);
    }

    @Override
    public void removeFilmDirectors(Long filmId) {
        String sql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}
