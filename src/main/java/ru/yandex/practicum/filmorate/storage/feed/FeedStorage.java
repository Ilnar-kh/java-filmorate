package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface FeedStorage {

    void addFriend(long userId, long friendToAddId) throws NotFoundException;

    void deleteFriend(long userId, long friendId) throws NotFoundException;

    void likeFromUser(long filmId, long userId);

    void unlikeFromUser(long filmId, long userId);

    void addReview(Review review);

    void deleteReview(Long reviewId, long userId);

    void updateReview(Review review);

    Collection<Feed> feeds(Long id);
}