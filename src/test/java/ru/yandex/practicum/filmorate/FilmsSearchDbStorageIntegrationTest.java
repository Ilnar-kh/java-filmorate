package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        UserDbStorage.class,
        FilmDbStorage.class,
        TestDataSetup.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
class FilmsSearchDbStorageIntegrationTest {
    private final FilmDbStorage filmDbStorage;
    @Autowired
    private final TestDataSetup testDataSetup;
    private final JdbcTemplate jdbcTemplate;


    // очистка таблиц и заполнение справочников
    @BeforeEach
    void setUp() {
        testDataSetup.cleanAllTables();
        testDataSetup.insertGenres();
        testDataSetup.insertMpa();
        testDataSetup.insertFriendshipStatuses();
    }


    // найти общие фильмы для двух пользователей,
    // вывести фильмы в порядке популярности(по лайкам)
    @Test
    void testCommonFilms() {
        // создаем пользователей и фильмы
        testDataSetup.insertTestUsers(5);    // Пользователи с Id 1-5
        testDataSetup.insertTestFilms(5);    // Фильмы с Id 1-5

        /*
         * Лайки:
         * Фильм 1 —> лайк от 1             → 1 лайк
         * Фильм 2 —> лайки от 2, 3         → 2 лайка
         * Фильм 3 —> лайков нет            → 0 лайка
         * Фильм 4 —> лайки от 1, 2         → 2 лайка
         * Фильм 5 —> лайки от 1, 2, 3      → 3 лайка
         */
        testDataSetup.insertFilmLikes(1L, List.of(1L));
        testDataSetup.insertFilmLikes(2L, List.of(2L, 3L));
        testDataSetup.insertFilmLikes(4L, List.of(1L, 2L));
        testDataSetup.insertFilmLikes(5L, List.of(1L, 2L, 3L));


        // ищем общие фильмы между user1 и user2
        List<Long> commonFilmIds = filmDbStorage.findCommonFilms(1L, 2L)
                .stream()
                .map(Film::getId)
                .toList();

        // проверка
        // Оба пользователя (1 и 2) лайкнули фильмы 5 и 4
        // Порядок по популярности (по убыванию лайков): 5 (3 лайка), затем 4 (2 лайка)
        List<Long> expectedFilmIds = List.of(5L, 4L);
        assertEquals(expectedFilmIds, commonFilmIds,
                "Общие фильмы должны быть отсортированы [5, 4] по убыванию количества лайков");


        // проверка симметрии: (2,1) должен вернуть то же самое, что и (1,2)
        // ищем общие фильмы между user2 и user1, т.е те же фильмы 5 и 4
        List<Long> reversedFilmIds = filmDbStorage.findCommonFilms(2L, 1L).stream()
                .map(Film::getId)
                .toList();

        assertEquals(expectedFilmIds, reversedFilmIds,
                "Результат должен быть одинаков при смене порядка пользователей");


        // Пользователи 1 и 5 не имеют общих лайков
        List<Film> noCommonFilms = filmDbStorage.findCommonFilms(1L, 5L);
        assertTrue(noCommonFilms.isEmpty(),
                "Если у пользователей нет общих лайков, список должен быть пустым");
    }

    @Test
    void testSearchByTitleAndDirector() {
        // Arrange: создаем пользователей, фильмы, справочники
        testDataSetup.insertTestUsers(3);       // Users 1-3
        testDataSetup.insertTestFilms(3);       // Films 1-3
        testDataSetup.insertDirectors(2);       // Directors 1-2

        // Фильм 1  "Inception" — режиссёр 1: Nolan
        // Фильм 2 "Jaws" — режиссёр 2: Spielberg


        // Обновляем имена режиссёров вручную
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE director_id = ?", "Christopher Nolan", 1);
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE director_id = ?", "Steven Spielberg", 2);

        // Привязка режиссёров:
        testDataSetup.insertFilmDirector(1, 1); // Nolan -> Film 1
        testDataSetup.insertFilmDirector(2, 2); // Spielberg -> Film 2

        // Обновляем названия фильмов
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "Inception", 1);
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "Jaws", 2);
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "Random Movie", 3);

        // Лайки:
        testDataSetup.insertFilmLikes(1L, List.of(1L, 2L));   // Inception — 2 лайка
        testDataSetup.insertFilmLikes(2L, List.of(3L));       // Jaws — 1 лайк
        // Random Movie — 0 лайков


        // Поиск по названию ("inception")
        List<Film> byTitle = filmDbStorage.searchByTitleAndDirector("inception");
        assertEquals(1, byTitle.size(), "Должен быть найден 1 фильм по названию");
        assertEquals("Inception", byTitle.get(0).getName());

        // Поиск по режиссёру ("spielberg")
        List<Film> byDirector = filmDbStorage.searchByTitleAndDirector("spielberg");
        assertEquals(1, byDirector.size(), "Должен быть найден 1 фильм по режиссёру");
        assertEquals("Jaws", byDirector.get(0).getName());

        // Поиск по части имени режиссёра ("nolan") — без учета регистра
        List<Film> byPartialDirector = filmDbStorage.searchByTitleAndDirector("NoLaN");
        assertEquals(1, byPartialDirector.size());
        assertEquals("Inception", byPartialDirector.get(0).getName());

        // Поиск по названию фильма с нулём лайков
        List<Film> noLikes = filmDbStorage.searchByTitleAndDirector("movie");
        assertEquals(1, noLikes.size());
        assertEquals("Random Movie", noLikes.get(0).getName());

        // Проверка порядка: при совпадении нескольких — сортировка по лайкам
        List<Film> multiple = filmDbStorage.searchByTitleAndDirector("a");
        List<String> titles = multiple.stream().map(Film::getName).toList();

        // Ожидаем порядок: Inception (2 лайка), Jaws (1), Random Movie (0)
        List<String> expected = List.of("Inception", "Jaws", "Random Movie");
        assertEquals(expected, titles, "Фильмы должны быть отсортированы по лайкам по убыванию");
    }

    @Test
    void testSearchByTitle() {
        // Arrange
        testDataSetup.insertTestUsers(2);
        testDataSetup.insertTestFilms(3);
        testDataSetup.insertDirectors(2);

        // Названия фильмов
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "The Prestige", 1);
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "The Matrix", 2);
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "Random Movie", 3);

        // Лайки
        testDataSetup.insertFilmLikes(1L, List.of(1L, 2L));  // Prestige – 2 лайка
        testDataSetup.insertFilmLikes(2L, List.of(1L));      // Matrix – 1 лайк

        // Act
        List<Film> result = filmDbStorage.searchByTitle("the");

        // Assert
        assertEquals(2, result.size(), "Ожидается 2 фильма с 'the' в названии");
        List<String> expected = List.of("The Prestige", "The Matrix"); // по убыванию лайков
        List<String> actual = result.stream().map(Film::getName).toList();
        assertEquals(expected, actual);
    }

    @Test
    void testSearchByDirector() {
        // Arrange
        testDataSetup.insertTestUsers(3);
        testDataSetup.insertTestFilms(3);
        testDataSetup.insertDirectors(2);

        // Установка имён режиссёров
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE director_id = ?", "Quentin Tarantino", 1);
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE director_id = ?", "James Cameron", 2);

        // Назначаем режиссёров
        testDataSetup.insertFilmDirector(1, 1); // Film 1 — Tarantino
        testDataSetup.insertFilmDirector(2, 2); // Film 2 — Cameron

        // Названия фильмов
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "Pulp Fiction", 1);
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "Titanic", 2);
        jdbcTemplate.update("UPDATE films SET name = ? WHERE id = ?", "No Director Film", 3); // без режиссёра

        // Лайки
        testDataSetup.insertFilmLikes(1L, List.of(1L, 2L, 3L)); // 3 лайка
        testDataSetup.insertFilmLikes(2L, List.of(1L));         // 1 лайк

        // Act
        List<Film> result = filmDbStorage.searchByDirector("tarantino");

        // Assert
        assertEquals(1, result.size(), "Должен быть найден только один фильм режиссёра Tarantino");
        assertEquals("Pulp Fiction", result.get(0).getName());
    }
}
