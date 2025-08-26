package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase
@Slf4j
public class FeedDbStorageTest {
    @Autowired
    private FeedDbStorage feedDbStorage;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        // Очищаем таблицу feeds перед каждым тестом
        String sqlQuery = "TRUNCATE TABLE feeds";
        jdbc.update(sqlQuery);
    }

    @Test
    public void testAddFriendShouldCreateFeedRecord() throws NotFoundException {
        // Arrange
        long userId = 1L;
        long friendId = 2L;

        // Act
        feedDbStorage.addFriend(userId, friendId);

        // Assert
        Collection<Feed> feeds = feedDbStorage.feeds(userId);
        assertEquals(1, feeds.size());

        Feed feed = feeds.iterator().next();
        assertEquals(userId, feed.getUserId());
        assertEquals("FRIEND", feed.getEventType());
        assertEquals("ADD", feed.getOperation());
        assertEquals(friendId, feed.getEntityId());
    }

    @Test
    public void testDeleteFriendShouldCreateFeedRecord() throws NotFoundException {
        // Arrange
        long userId = 1L;
        long friendId = 2L;

        // Act
        feedDbStorage.deleteFriend(userId, friendId);

        // Assert
        Collection<Feed> feeds = feedDbStorage.feeds(userId);
        assertEquals(1, feeds.size());

        Feed feed = feeds.iterator().next();
        assertNotNull(feed.getTimestamp());
        assertEquals(userId, feed.getUserId());
        assertEquals("FRIEND", feed.getEventType());
        assertEquals("REMOVE", feed.getOperation());
        assertEquals(friendId, feed.getEntityId());
    }

    @Test
    public void testLikeFromUserShouldCreateFeedRecord() {
        // Arrange
        long filmId = 1L;
        long userId = 1L;

        // Act
        feedDbStorage.likeFromUser(filmId, userId);

        // Assert
        Collection<Feed> feeds = feedDbStorage.feeds(userId);
        assertEquals(1, feeds.size());

        Feed feed = feeds.iterator().next();
        assertEquals(userId, feed.getUserId());
        assertEquals("LIKE", feed.getEventType());
        assertEquals("ADD", feed.getOperation());
        assertEquals(filmId, feed.getEntityId());
    }

    @Test
    public void testUnlikeFromUserShouldCreateFeedRecord() {
        // Arrange
        long filmId = 1L;
        long userId = 1L;

        // Act
        feedDbStorage.unlikeFromUser(filmId, userId);

        // Assert
        Collection<Feed> feeds = feedDbStorage.feeds(userId);
        assertEquals(1, feeds.size());

        Feed feed = feeds.iterator().next();
        assertEquals(userId, feed.getUserId());
        assertEquals("LIKE", feed.getEventType());
        assertEquals("REMOVE", feed.getOperation());
        assertEquals(filmId, feed.getEntityId());
    }

    @Test
    public void testAddReviewShouldCreateFeedRecord() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setUserId(1L);
        review.setContent("Тестовый отзыв");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUseful(0);

        // Act
        feedDbStorage.addReview(review);

        // Assert
        Collection<Feed> feeds = feedDbStorage.feeds(review.getUserId());
        assertEquals(1, feeds.size());

        Feed feed = feeds.iterator().next();
        assertNotNull(feed.getTimestamp());
        assertEquals(review.getUserId(), feed.getUserId());
        assertEquals("REVIEW", feed.getEventType());
        assertEquals("ADD", feed.getOperation());
        assertEquals(review.getId(), feed.getEntityId());
    }

    @Test
    public void testUpdateReviewShouldCreateFeedRecord() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setUserId(1L);
        review.setContent("Тестовый отзыв");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUseful(0);

        // Act
        feedDbStorage.updateReview(review);

        // Assert
        Collection<Feed> feeds = feedDbStorage.feeds(review.getUserId());
        assertEquals(1, feeds.size());

        Feed feed = feeds.iterator().next();
        assertNotNull(feed.getTimestamp());
        assertEquals(review.getUserId(), feed.getUserId());
        assertEquals("REVIEW", feed.getEventType());
        assertEquals("UPDATE", feed.getOperation());
        assertEquals(review.getId(), feed.getEntityId());
    }

    @Test
    public void testDeleteReviewShouldCreateFeedRecord() {
        // Arrange
        long reviewId = 1L;
        long userId = 1L;

        // Act
        feedDbStorage.deleteReview(reviewId, userId);

        // Assert
        Collection<Feed> feeds = feedDbStorage.feeds(userId);
        assertEquals(1, feeds.size());

        Feed feed = feeds.iterator().next();
        assertNotNull(feed.getTimestamp());
        assertEquals(userId, feed.getUserId());
        assertEquals("REVIEW", feed.getEventType());
        assertEquals("REMOVE", feed.getOperation());
        assertEquals(reviewId, feed.getEntityId());
    }
}