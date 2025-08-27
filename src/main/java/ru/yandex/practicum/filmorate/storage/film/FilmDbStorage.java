package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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
    public Optional<Film> update(Film film) {
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
        List<Film> films = jdbcTemplate.query(selectAllQuery, this::mapRowToFilm);
        if (films.isEmpty()) return films;
        Set<Long> ids = films.stream().map(Film::getId).collect(java.util.stream.Collectors.toSet());
        Map<Long, Set<Genre>> genresByFilm = loadGenresByFilmIds(ids);
        for (Film f : films) {
            f.setGenres(genresByFilm.getOrDefault(f.getId(), java.util.Collections.emptySet()));
        }
        return films;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
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
        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, filmId);
            Map<Long, Set<Genre>> genresByFilm = loadGenresByFilmIds(java.util.Set.of(film.getId()));
            film.setGenres(new java.util.LinkedHashSet<>(
                    genresByFilm.getOrDefault(film.getId(), java.util.Collections.emptySet())
            ));
            return java.util.Optional.of(film);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return java.util.Optional.empty();
        }
    }

    @Override
    public List<Film> findFilmsByIds(Set<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = """
                    SELECT f.id,
                           f.name,
                           f.description,
                           f.release_date,
                           f.duration,
                           f.mpa_id,
                           m.name AS mpa_name
                    FROM films f
                    LEFT JOIN mpa m ON f.mpa_id = m.id
                    WHERE f.id IN (%s)
                """.formatted(placeholders);

        return jdbcTemplate.query(sql, this::mapRowToFilm, filmIds.toArray());
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
                .genres(new LinkedHashSet<>()) // Используем LinkedHashSet
                .build();

        return film;
    }

    public void saveFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        // Сохраняем порядок жанров, указанный в запросе
        List<Genre> genres = new ArrayList<>(film.getGenres());
        // Удаляем дубликаты, сохраняя порядок первого вхождения
        List<Genre> uniqueGenres = genres.stream()
                .filter(genre -> genre != null && genre.getId() != null)
                .distinct()
                .toList();

        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(
                insertSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement pstmt, int i) throws SQLException {
                        pstmt.setLong(1, film.getId());
                        pstmt.setInt(2, uniqueGenres.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return uniqueGenres.size();
                    }
                }
        );
    }

    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, COUNT(fl.user_id) AS likes
                FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                LEFT JOIN mpa m ON f.mpa_id = m.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                ORDER BY likes DESC
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);

        if (films.isEmpty()) return films;

        loadGenresForFilms(films);

        return films;
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        // 1. Получаем ID популярных фильмов
        List<Long> topIds = fetchPopularIds(count, genreId, year);
        if (topIds.isEmpty()) return Collections.emptyList();

        // 2. Загружаем фильмы
        String placeholders = topIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String filmSql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON m.id = f.mpa_id
                WHERE f.id IN (""" + placeholders + ")";

        List<Film> films = jdbcTemplate.query(filmSql, this::mapRowToFilm, topIds.toArray());

        // Сохраняем порядок
        Map<Long, Integer> rank = new HashMap<>();
        for (int i = 0; i < topIds.size(); i++) {
            rank.put(topIds.get(i), i);
        }
        films.sort(Comparator.comparingInt(f -> rank.getOrDefault(f.getId(), Integer.MAX_VALUE)));

        // 3. Подгружаем жанры
        Set<Long> ids = films.stream().map(Film::getId).collect(Collectors.toSet());
        Map<Long, Set<Genre>> genresByFilm = loadGenresByFilmIds(ids);
        for (Film f : films) {
            f.setGenres(genresByFilm.getOrDefault(f.getId(), Collections.emptySet()));
        }

        // 4. Подгружаем режиссёров
        Map<Long, Set<Director>> directorsByFilm = loadDirectorsByFilmIds(ids);
        for (Film f : films) {
            Set<Director> directors = directorsByFilm.getOrDefault(f.getId(), Collections.emptySet());
            f.setDirectors(new ArrayList<>(directors));
        }

        return films;
    }

    private List<Long> fetchPopularIds(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.id
                FROM films f
                LEFT JOIN film_likes fl ON fl.film_id = f.id
                """);

        List<Object> params = new ArrayList<>();
        boolean whereAdded = false;

        if (genreId != null) {
            sql.append("JOIN film_genres fg ON fg.film_id = f.id ");
        }

        if (genreId != null) {
            sql.append(whereAdded ? "AND " : "WHERE ");
            sql.append("fg.genre_id = ? ");
            params.add(genreId);
            whereAdded = true;
        }

        if (year != null) {
            sql.append(whereAdded ? "AND " : "WHERE ");
            sql.append("EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
            whereAdded = true;
        }

        sql.append("""
                GROUP BY f.id
                ORDER BY COUNT(fl.user_id) DESC, f.id
                LIMIT ?
                """);
        params.add(count);

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rn) -> rs.getLong(1),
                params.toArray()
        );
    }

    private Map<Long, Set<Genre>> loadGenresByFilmIds(Set<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id IN (""" + placeholders + ")";

        Map<Long, Set<Genre>> result = new HashMap<>();
        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        });

        return result;
    }

    private Map<Long, Set<Director>> loadDirectorsByFilmIds(Set<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                SELECT fd.film_id, d.director_id, d.name
                FROM film_directors fd
                JOIN directors d ON d.director_id = fd.director_id
                WHERE fd.film_id IN (""" + placeholders + ")";

        Map<Long, Set<Director>> result = new HashMap<>();

        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            long filmId = rs.getLong("film_id");
            Director director = new Director(
                    rs.getLong("director_id"),
                    rs.getString("name")
            );
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(director);
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
            loadGenresForFilms(films);
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
            loadGenresForFilms(films);
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

    public int removeById(Long filmId) {
        return jdbcTemplate.update("DELETE FROM films WHERE id = ?", filmId);
    }

    @Override
    public List<Film> findCommonFilms(Long userId, Long friendId) {
        String sql = """
                SELECT f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                m.name AS mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                JOIN (
                     SELECT film_id
                     FROM film_likes
                     WHERE user_id IN (?, ?)
                     GROUP BY film_id
                     HAVING COUNT(user_id) = 2
                ) common_likes ON common_likes.film_id = f.id
                JOIN (
                     SELECT film_id, COUNT(*) AS total_likes
                     FROM film_likes
                     GROUP BY film_id
                ) fl ON fl.film_id = f.id
                ORDER BY fl.total_likes DESC
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, userId, friendId);

        if (films.isEmpty()) return films;

        loadGenresForFilms(films);

        return films;
    }

    @Override
    public List<Film> searchByTitle(String query) {
        String sql = """
                SELECT f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                m.name AS mpa_name,
                fl.total_likes
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN (
                       SELECT film_id, COUNT(*) AS total_likes
                       FROM film_likes
                       GROUP BY film_id
                ) fl ON fl.film_id = f.id
                WHERE LOWER(f.name) LIKE ?
                ORDER BY fl.total_likes DESC
                """;
        String likeQuery = "%" + query.toLowerCase() + "%";


        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, likeQuery);

        if (films.isEmpty()) return films;

        loadGenresForFilms(films);

        return films;
    }

    @Override
    public List<Film> searchByDirector(String query) {
        String sql = """
                SELECT DISTINCT f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                m.name AS mpa_name,
                fl.total_likes
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                JOIN film_directors fd ON f.id = fd.film_id
                JOIN directors d ON fd.director_id = d.director_id
                LEFT JOIN (
                       SELECT film_id, COUNT(*) AS total_likes
                       FROM film_likes
                       GROUP BY film_id
                ) fl ON fl.film_id = f.id
                WHERE LOWER(d.name) LIKE ?
                ORDER BY fl.total_likes DESC
                """;
        String likeQuery = "%" + query.toLowerCase() + "%";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, likeQuery);
        if (films.isEmpty()) return films;

        loadGenresForFilms(films);

        return films;
    }

    @Override
    public List<Film> searchByTitleAndDirector(String query) {
        String sql = """
                SELECT DISTINCT f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                m.name AS mpa_name,
                fl.total_likes
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.director_id
                LEFT JOIN (
                       SELECT film_id, COUNT(*) AS total_likes
                       FROM film_likes
                       GROUP BY film_id
                ) fl ON fl.film_id = f.id
                WHERE LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?
                ORDER BY fl.total_likes DESC
                """;
        String likeQuery = "%" + query.toLowerCase() + "%";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, likeQuery, likeQuery);
        if (films.isEmpty()) return films;

        loadGenresForFilms(films);

        return films;
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        // Собираем ID фильмов
        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        // Одним запросом тянем все жанры
        Map<Long, Set<Genre>> genresByFilm = loadGenresByFilmIds(filmIds);

        // Расставляем жанры для каждого фильма
        for (Film film : films) {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), Collections.emptySet()));
        }
    }
}
