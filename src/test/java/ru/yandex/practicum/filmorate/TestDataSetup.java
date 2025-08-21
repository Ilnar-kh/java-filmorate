package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/*
 * Класс хранит шаблоны для подготовки тестовых данных.
 */
@Component
@Profile("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestDataSetup {

    private final JdbcTemplate jdbcTemplate;

    /* ==================== Очистка всех таблиц ==================== */
    public void cleanAllTables() {
        // Отключаем проверки внешних ключей (для H2), чтобы удалять таблицы в любом порядке
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        // Получаем список всех таблиц из метаданных
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema='PUBLIC'", String.class);

        // Удаляем все данные из таблиц
        tables.stream()
              .forEach(t -> jdbcTemplate.execute("TRUNCATE TABLE " + t));

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE"); // Включаем проверки обратно
    }

    /* ==================== Заполнение жанров ==================== */
    public void insertGenres() {
        List<Object[]> genres = List.of(
                new Object[]{1, "Комедия"},
                new Object[]{2, "Драма"},
                new Object[]{3, "Мультфильм"},
                new Object[]{4, "Триллер"},
                new Object[]{5, "Документальный"},
                new Object[]{6, "Боевик"}
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO genres (id, name) VALUES (?, ?)",
                genres
        );
    }

    /* ==================== Заполнение mpa ==================== */
    public void insertMpa() {
        List<Object[]> mpaRatings = List.of(
                new Object[]{1, "G"},
                new Object[]{2, "PG"},
                new Object[]{3, "PG-13"},
                new Object[]{4, "R"},
                new Object[]{5, "NC-17"}
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO mpa (id, name) VALUES (?, ?)",
                mpaRatings
        );
    }

    /* ==================== Заполнение статуса дружбы ==================== */
    public void insertFriendshipStatuses() {
        List<Object[]> statuses = List.of(
                new Object[]{"PENDING", "Запрос отправлен"},
                new Object[]{"CONFIRMED", "В дружбе"},
                new Object[]{"DECLINED", "Отклонено"}
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO friendship_statuses (code, description) VALUES (?, ?)",
                statuses
        );
    }


    /* ==================== Заполнение пользователей ==================== */
    public void insertTestUsers(int count) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int id = i + 1;
                        ps.setInt(1, id);
                        ps.setString(2, "user" + id + "@mail.ru");
                        ps.setString(3, "login" + id);
                        ps.setString(4, "Name " + id);
                        ps.setDate(5, Date.valueOf(LocalDate.now().minusYears(20 + i)));
                    }

                    @Override
                    public int getBatchSize() {
                        return count;
                    }
                }
        );
    }

    /* ==================== Заполнение друзей ==================== */
    public void insertFriends(Long requesterId, List<Long> addresseeIds) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO user_friends (requester_id, addressee_id, status_code) VALUES (?, ?, 'CONFIRMED')",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, requesterId);
                        ps.setLong(2, addresseeIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return addresseeIds.size();
                    }
                }
        );
    }

    // когда нужен 1 друг
    public void insertFriend(Long requesterId, Long addresseeId) {
        jdbcTemplate.update(
                "INSERT INTO user_friends (requester_id, addressee_id, status_code) VALUES (?, ?, 'CONFIRMED')",
                requesterId, addresseeId
        );
    }

    /* ==================== Заполнение фильмов ==================== */
    public void insertTestFilms(int count) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO films (id, name, description, release_date, duration, mpa_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int id = i + 1;
                        ps.setInt(1, id);
                        ps.setString(2, "Фильм " + id);
                        ps.setString(3, "Описание фильма " + id);
                        ps.setDate(4, Date.valueOf(LocalDate.now().minusYears(i)));
                        ps.setInt(5, 90 + i);
                        ps.setInt(6, (i % 5) + 1); // MPA 1..5
                    }

                    @Override
                    public int getBatchSize() {
                        return count;
                    }
                }
        );
        // Добавляем фиксированные связи с жанрами
        // Каждый фильм получает жанр с ID = (film_id % 6) + 1
        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        int filmId = i + 1;
                        int genreId = (filmId % 6) + 1; // Жанры 1..6
                        ps.setInt(1, filmId);
                        ps.setInt(2, genreId);
                    }

                    @Override
                    public int getBatchSize() {
                        return count;
                    }
                }
        );
    }

    /* ==================== Добавление лайков фильму ==================== */
    public void insertFilmLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                filmId, userId
        );
    }

    public void insertFilmLikes(Long filmId, List<Long> userIds) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, filmId);
                        ps.setLong(2, userIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return userIds.size();
                    }
                }
        );
    }
}
