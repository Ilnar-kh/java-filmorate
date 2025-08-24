package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

@Slf4j
@Component("feedDbStorage")
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public void addFriend(long userId, long friendToAddId) throws NotFoundException {
        String action = "ADD";
        addOrRemoveFriend(userId, friendToAddId, action);
    }


    public void deleteFriend(long userId, long friendId) throws NotFoundException {
        String action = "REMOVE";
        addOrRemoveFriend(userId, friendId, action);
    }


    public void likeFromUser(long filmId, long userId) {
        String action = "ADD";
        addOrRemoveLikeFromUser(userId, filmId, action);
    }


    public void unlikeFromUser(long filmId, long userId) {
        String action = "REMOVE";
        addOrRemoveLikeFromUser(userId, filmId, action);
    }

    public void addReview(Review review) {
        String action = "ADD";
        addOrUpdateReview(review, action);
    }

    public void updateReview(Review review) {
        String action = "UPDATE";
        addOrUpdateReview(review, action);
    }


    public void deleteReview(Long reviewId, long userId) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String sqlQuery =
                "insert into feeds(userId, timestamp, eventType, operation, entityId)" +
                        " values(?,?, ?, ?,?)";
        jdbcTemplate.update(sqlQuery, userId, timestamp.getTime(), "REVIEW", "REMOVE", reviewId);

    }

    public Collection<Feed> feeds(Long id) {
        String sqlQuery = "select timestamp, userId, eventType, operation, eventId, entityId from feeds where userId = ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFeed, id);
    }

    private Feed mapRowToFeed(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getInt("userId"))
                .eventType(rs.getString("eventType"))
                .operation(rs.getString("operation"))
                .eventId(rs.getInt("eventId"))
                .entityId(rs.getInt("entityId"))
                .build();
    }

    private void addOrRemoveLikeFromUser(long userId, long filmId, String action) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String sqlQuery = "insert into feeds(userId, timestamp, eventType, operation, entityId)" +
                "values(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery, userId, timestamp.getTime(), "LIKE", action, filmId);
    }

    private void addOrUpdateReview(Review review, String action) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String sqlQuery =
                "insert into feeds(userId, timestamp, eventType, operation, entityId)" +
                        " values(?,?, ?, ?,?)";
        jdbcTemplate.update(sqlQuery, review.getUserId(), timestamp.getTime(), "REVIEW", action, review.getId());
    }

    private void addOrRemoveFriend(long userId, long friendId, String action) throws NotFoundException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String sqlQuery =
                "insert into feeds(userId, timestamp, eventType, operation, entityId)\n" +
                        "values(?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sqlQuery, userId, timestamp.getTime(), "FRIEND", action, friendId);
        } catch (DataAccessException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
