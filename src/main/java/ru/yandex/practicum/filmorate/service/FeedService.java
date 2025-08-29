package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public Collection<Feed> feeds(Long id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        return feedStorage.feeds(id).stream()
                .sorted(Comparator
                        .comparing(Feed::getTimestamp)
                        .thenComparing(Feed::getEventId))
                .collect(Collectors.toList());
    }

    public void addFriend(long userId, long friendToAddId) {
        feedStorage.addFriend(userId, friendToAddId);
    }

    public void deleteFriend(long userId, long friendId) {
        feedStorage.deleteFriend(userId, friendId);
    }

    public void likeFromUser(long filmId, long userId) {
        feedStorage.likeFromUser(filmId, userId);
    }

    public void unlikeFromUser(long filmId, long userId) {
        feedStorage.unlikeFromUser(filmId, userId);
    }

    public void deleteReview(long reviewId, Long userId) {
        feedStorage.deleteReview(reviewId, userId);
    }

    public void addReview(Review review) {
        feedStorage.addReview(review);
    }

    public void updateReview(Review review) {
        feedStorage.updateReview(review);
    }
}
