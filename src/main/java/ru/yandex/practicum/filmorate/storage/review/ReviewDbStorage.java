package ru.yandex.practicum.filmorate.storage.review;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
@AllArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getLong("useful"))
                .build();
    }

    @Override
    public Review create(Review review) {
        final String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?,?,?,?)";

        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive()); // у тебя Boolean isPositive -> getIsPositive()
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, kh);

        review.setId(kh.getKey().longValue());
        return review;
    }

    @Override
    public Review findById(Long reviewId) {
        final String sql = "SELECT * FROM reviews WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToReview, reviewId);
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        long id = review.getId();
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), id);
        return findById(id);
    }

    @Override
    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
    }

    @Override
    public List<Review> getReviewsByFilmId(Long id, int count) {
        if (id == null) {
            String sql = """
                        SELECT r.* FROM reviews r
                        ORDER BY r.useful DESC
                        LIMIT ?
                    """;
            return jdbcTemplate.query(sql, this::mapRowToReview, count);
        } else {
            String sql = """
                        SELECT r.* FROM reviews r
                        WHERE r.film_id = ?
                        ORDER BY r.useful DESC
                        LIMIT ?
                    """;
            return jdbcTemplate.query(sql, this::mapRowToReview, id, count);
        }
    }

    @Override
    public void putRate(Long id, Long userId, Boolean isUseful) {
        Boolean prev = jdbcTemplate.query(
                "SELECT is_useful FROM review_likes WHERE review_id = ? AND user_id = ?",
                rs -> rs.next() ? rs.getBoolean(1) : null,
                id, userId
        );

        if (prev == null) {
            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id, is_useful) VALUES (?, ?, ?)",
                    id, userId, isUseful
            );
            jdbcTemplate.update("UPDATE reviews SET useful = useful " + (isUseful ? "+ 1" : "- 1") + " WHERE id = ?", id);
        } else if (!prev.equals(isUseful)) {
            jdbcTemplate.update(
                    "UPDATE review_likes SET is_useful = ? WHERE review_id = ? AND user_id = ?",
                    isUseful, id, userId
            );
            jdbcTemplate.update("UPDATE reviews SET useful = useful " + (isUseful ? "+ 2" : "- 2") + " WHERE id = ?", id);
        }
    }


    @Override
    public void deleteRate(Long id, Long userId, Boolean isUseful) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_useful = ?",
                id, userId, isUseful
        );
        if (deleted > 0) {
            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful " + (isUseful ? "- 1" : "+ 1") + " WHERE id = ?",
                    id
            );
        }
    }
}

