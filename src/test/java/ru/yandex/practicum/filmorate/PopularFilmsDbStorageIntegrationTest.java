package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class PopularFilmsDbStorageIntegrationTest {

    private final FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        // Чистим в корректном порядке из-за FK
        jdbc.update("DELETE FROM film_likes");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM genres");
        jdbc.update("DELETE FROM mpa");

        // Справочники
        jdbc.update("INSERT INTO mpa(id, name) VALUES (1,'G'),(2,'PG'),(3,'PG-13')");
        jdbc.update("INSERT INTO genres(id, name) VALUES (1,'Комедия'),(2,'Драма'),(3,'Боевик')");

        // Пользователи (для лайков)
        jdbc.update("""
                    INSERT INTO users(id, email, login, name, birthday) VALUES
                    (1,'u1@ex.com','u1','U1','1990-01-01'),
                    (2,'u2@ex.com','u2','U2','1991-01-01'),
                    (3,'u3@ex.com','u3','U3','1992-01-01'),
                    (4,'u4@ex.com','u4','U4','1993-01-01'),
                    (5,'u5@ex.com','u5','U5','1994-01-01')
                """);

        // Фильмы (разные годы, разные MPA)
        jdbc.update("""
                    INSERT INTO films(id, name, description, release_date, duration, mpa_id) VALUES
                    (10,'F10','d','2020-02-01',100,2),
                    (20,'F20','d','2020-06-01',110,2),
                    (30,'F30','d','2021-03-01',120,3),
                    (40,'F40','d','2021-07-01',130,1)
                """);

        // Связь фильм-жанр
        jdbc.update("INSERT INTO film_genres(film_id, genre_id) VALUES (10,1)");
        jdbc.update("INSERT INTO film_genres(film_id, genre_id) VALUES (20,2)");
        jdbc.update("INSERT INTO film_genres(film_id, genre_id) VALUES (30,1)");
        jdbc.update("INSERT INTO film_genres(film_id, genre_id) VALUES (30,3)");
        jdbc.update("INSERT INTO film_genres(film_id, genre_id) VALUES (40,2)");

        // Лайки: делаем разные уровни популярности
        // F30: 4 лайка
        jdbc.update("INSERT INTO film_likes(film_id, user_id) VALUES (30,1),(30,2),(30,3),(30,4)");
        // F20: 3 лайка
        jdbc.update("INSERT INTO film_likes(film_id, user_id) VALUES (20,1),(20,2),(20,3)");
        // F10: 2 лайка
        jdbc.update("INSERT INTO film_likes(film_id, user_id) VALUES (10,1),(10,2)");
        // F40: 1 лайк
        jdbc.update("INSERT INTO film_likes(film_id, user_id) VALUES (40,5)");
    }

    @Test
    void popular_withoutFilters_returnsByLikesDesc() {
        List<Film> films = filmDbStorage.getPopularFilms(10, null, null);

        // порядок по убыванию лайков: 30(4), 20(3), 10(2), 40(1)
        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(30L, 20L, 10L, 40L);
    }

    @Test
    void popular_filterByGenre_onlyThisGenreOrderedByLikes() {
        // жанр 1 («Комедия») -> F30(4), F10(2)
        List<Film> films = filmDbStorage.getPopularFilms(10, 1, null);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(30L, 10L);
    }

    @Test
    void popular_filterByYear_onlyThisYearOrderedByLikes() {

        // год 2020 -> F20(3), F10(2)
        List<Film> films = filmDbStorage.getPopularFilms(10, null, 2020);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(20L, 10L);
    }

    @Test
    void popular_filterByGenreAndYear_intersectionOnly() {
        // год 2021 + жанр 2 («Драма») -> только F40(1)
        List<Film> films = filmDbStorage.getPopularFilms(10, 2, 2021);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(40L);
    }

    @Test
    void popular_respectsCountLimit() {
        List<Film> films = filmDbStorage.getPopularFilms(2, null, null);

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getId)
                .containsExactly(30L, 20L);
    }

    @Test
    void popular_returnsEmpty_whenNoMatches() {
        // В 2020 нет фильмов жанра 3 («Боевик»)
        List<Film> films = filmDbStorage.getPopularFilms(10, 3, 2020);

        assertThat(films).isEmpty();
    }
}
