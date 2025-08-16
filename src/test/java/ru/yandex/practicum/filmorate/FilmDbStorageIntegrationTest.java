package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageIntegrationTest {

    private final FilmDbStorage filmDbStorage;
    private final MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void testCreateFindByIdAndFindAll() {
        // Получаем рейтинг с кодом "PG"
        MpaRating rating = mpaDbStorage.findById(2);

        Film filmToCreate = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120L)
                .mpa(rating)
                .build();

        Film createdFilm = filmDbStorage.create(filmToCreate);
        assertThat(createdFilm.getId()).isPositive();

        Film retrievedFilm = filmDbStorage.findById(createdFilm.getId());
        assertThat(retrievedFilm).isNotNull()
                .extracting("id", "name", "description", "duration", "mpa.id", "mpa.name")
                .containsExactly(
                        createdFilm.getId(),
                        "Название",
                        "Описание",
                        120L,
                        rating.getId(),
                        rating.getName()
                );

        List<Film> allFilms = filmDbStorage.findAll();
        assertThat(allFilms).extracting("id").contains(createdFilm.getId());
    }

    @Test
    void testUpdateFilm() {
        MpaRating rating = mpaDbStorage.findById(1);

        Film originalFilm = Film.builder()
                .name("Old Name")
                .description("Old Description")
                .releaseDate(LocalDate.now())
                .duration(90L)
                .mpa(rating)
                .build();

        Film createdFilm = filmDbStorage.create(originalFilm);

        // Обновляем
        createdFilm.setName("New Name");
        createdFilm.setDuration(100L);
        MpaRating rRating = mpaDbStorage.findById(4);
        createdFilm.setMpa(rRating);

        Film updatedFilm = filmDbStorage.update(createdFilm);
        Film retrievedFilm = filmDbStorage.findById(updatedFilm.getId());

        assertThat(retrievedFilm.getName()).isEqualTo("New Name");
        assertThat(retrievedFilm.getDuration()).isEqualTo(100L);
        assertThat(retrievedFilm.getMpa().getId()).isEqualTo(4);
    }

    @Test
    void testLikesFlow() {
        // Добавляем пользователя напрямую
        jdbcTemplate.update(
                "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user@example.com", "userLogin", "User Name", LocalDate.of(2000, 1, 1)
        );

        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE login = ?", Long.class, "userLogin");

        MpaRating rating = mpaDbStorage.findById(3);

        Film film = Film.builder()
                .name("Some Film")
                .description("Some Description")
                .releaseDate(LocalDate.of(2021, 5, 15))
                .duration(100L)
                .mpa(rating)
                .build();

        Film createdFilm = filmDbStorage.create(film);

        // Лайк
        filmDbStorage.addLike(createdFilm.getId(), userId);
        Integer likeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class, createdFilm.getId(), userId
        );
        assertThat(likeCount).isEqualTo(1);

        // Удаляем лайк
        filmDbStorage.removeLike(createdFilm.getId(), userId);
        Integer likeCountAfterRemove = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class, createdFilm.getId(), userId
        );
        assertThat(likeCountAfterRemove).isZero();
    }
}
