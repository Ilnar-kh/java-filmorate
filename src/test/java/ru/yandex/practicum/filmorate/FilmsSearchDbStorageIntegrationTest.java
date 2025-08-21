package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
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
}
