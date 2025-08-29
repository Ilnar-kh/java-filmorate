package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(ReviewDbStorage.class)
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
class ReviewDbStorageTest {

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ReviewDbStorage storage;

    private Long filmId;
    private Long user1;
    private Long user2;

    @BeforeEach
    void initData() {

        jdbc.update("DELETE FROM review_likes");
        jdbc.update("DELETE FROM reviews");
        jdbc.update("DELETE FROM film_likes");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");

        jdbc.update("INSERT INTO users(email, login, name, birthday) VALUES (?,?,?,?)",
                "u1@y.ru", "u1", "U1", LocalDate.of(1990, 1, 1));
        jdbc.update("INSERT INTO users(email, login, name, birthday) VALUES (?,?,?,?)",
                "u2@y.ru", "u2", "U2", LocalDate.of(1991, 2, 2));

        user1 = jdbc.queryForObject("SELECT id FROM users WHERE login='u1'", Long.class);
        user2 = jdbc.queryForObject("SELECT id FROM users WHERE login='u2'", Long.class);

        jdbc.update("INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?,?,?,?,?)",
                "F1", "desc", LocalDate.of(2000, 1, 1), 100, 2 /* PG */);
        filmId = jdbc.queryForObject("SELECT id FROM films WHERE name='F1'", Long.class);

        jdbc.update("INSERT INTO reviews(content, is_positive, user_id, film_id, useful) VALUES (?,?,?,?,0)",
                "ok", true, user1, filmId);
    }

    private Long getBaseReviewId() {
        return jdbc.queryForObject("SELECT id FROM reviews WHERE user_id = ? AND film_id = ?",
                Long.class, user1, filmId);
    }

    @Test
    @DisplayName("create/findById/update работают корректно")
    void createFindUpdate() {
        Review created = storage.create(Review.builder()
                .content("great")
                .isPositive(true)
                .userId(user1)
                .filmId(filmId)
                .build());

        assertThat(created.getId()).isNotNull();

        Review found = storage.findById(created.getId());
        assertThat(found.getContent()).isEqualTo("great");
        assertThat(found.getUseful()).isZero();

        found.setContent("updated");
        found.setIsPositive(false);
        Review updated = storage.update(found);

        assertThat(updated.getContent()).isEqualTo("updated");
        assertThat(updated.getIsPositive()).isFalse();
    }

    @Test
    @DisplayName("getReviewsByFilmId сортирует по useful убыв.")
    void getReviewsByFilmIdSorted() {
        Long r0 = getBaseReviewId(); // useful = 0

        Review r1 = storage.create(Review.builder()
                .content("r1").isPositive(true).userId(user1).filmId(filmId).build());
        Review r2 = storage.create(Review.builder()
                .content("r2").isPositive(false).userId(user1).filmId(filmId).build());

        // накрутим useful
        jdbc.update("UPDATE reviews SET useful = 5 WHERE id = ?", r1.getId());
        jdbc.update("UPDATE reviews SET useful = 2 WHERE id = ?", r0);
        jdbc.update("UPDATE reviews SET useful = -3 WHERE id = ?", r2.getId());

        List<Review> list = storage.getReviewsByFilmId(filmId, 10);
        assertThat(list).extracting(Review::getId)
                .containsExactly(r1.getId(), r0, r2.getId());
    }

    @Test
    @DisplayName("putRate: первый лайк -> +1; первый дизлайк -> -1")
    void putRateFirstVote() {
        Long id = getBaseReviewId();

        storage.putRate(id, user2, true); // лайк
        assertThat(storage.findById(id).getUseful()).isEqualTo(1);

        storage.putRate(id, user1, false); // дизлайк другим пользователем
        assertThat(storage.findById(id).getUseful()).isEqualTo(0); // 1 + (-1)
    }

    @Test
    @DisplayName("putRate: смена лайк→дизлайк даёт -2; дизлайк→лайк даёт +2")
    void putRateToggleVote() {
        Long id = getBaseReviewId();

        storage.putRate(id, user2, true); // +1
        assertThat(storage.findById(id).getUseful()).isEqualTo(1);

        storage.putRate(id, user2, false); // -2 => -1
        assertThat(storage.findById(id).getUseful()).isEqualTo(-1);

        storage.putRate(id, user2, true); // +2 => +1
        assertThat(storage.findById(id).getUseful()).isEqualTo(1);
    }

    @Test
    @DisplayName("deleteRate: удаление лайка -1, удаление дизлайка +1")
    void deleteRate() {
        Long id = getBaseReviewId();

        storage.putRate(id, user2, true); // +1
        storage.putRate(id, user1, false); // -1 → итог 0

        storage.deleteRate(id, user2, true); // снять лайк: 0 - 1 = -1
        assertThat(storage.findById(id).getUseful()).isEqualTo(-1);

        storage.deleteRate(id, user1, false); // снять дизлайк: -1 + 1 = 0
        assertThat(storage.findById(id).getUseful()).isEqualTo(0);
    }
}
