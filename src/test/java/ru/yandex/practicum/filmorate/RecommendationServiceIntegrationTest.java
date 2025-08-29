package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RecommendationServiceIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @BeforeEach
    void setupDb() {
        // чистим таблицы перед каждым тестом
        jdbc.update("DELETE FROM film_likes");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM mpa");

        // Добавляем рейтинги MPA
        jdbc.update("INSERT INTO mpa(id, name) VALUES (1,'G'),(2,'PG'),(3,'PG-13')");

        // фильмы
        jdbc.update("INSERT INTO films(id, name, description, release_date, duration, mpa_id) VALUES " +
                "(10,'Film10','d', '2020-01-01', 100, 2)," +
                "(20,'Film20','d', '2020-01-02', 110, 2)," +
                "(30,'Film30','d', '2020-01-03', 120, 3)");

        // пользователи
        jdbc.update("INSERT INTO users(id, email, login, name, birthday) VALUES " +
                "(1,'a@a.a','a','UserA','1990-01-01')," +
                "(2,'b@b.b','b','UserB','1992-02-02')");

        // лайки:
        // u1 -> {10,20}
        // u2 -> {20,30}
        // пересечение = {20}, рекомендация для u1 = {30}
        jdbc.update("INSERT INTO film_likes(film_id, user_id) VALUES (10,1),(20,1),(20,2),(30,2)");
    }

    @Test
    void shouldReturnRecommendationsBasedOnMostSimilarUser() {
        List<Film> recs = recommendationService.getRecommendations(1L);

        assertThat(recs)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(30L);
    }

    @Test
    void shouldReturnEmptyListIfNoOverlap() {
        // чистим лайки
        jdbc.update("DELETE FROM film_likes");

        // ставим лайк только одному фильму пользователем 1
        jdbc.update("INSERT INTO film_likes(film_id, user_id) VALUES (10,1)");

        List<Film> recs = recommendationService.getRecommendations(1L);

        assertThat(recs).isEmpty();
    }
}
