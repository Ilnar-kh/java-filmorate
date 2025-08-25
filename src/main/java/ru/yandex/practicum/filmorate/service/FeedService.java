package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedStorage feedStorage;

    //Возвращает ленту событий пользователя
    public Collection<Feed> feeds(Long id) {
        return feedStorage.feeds(id);
    }

    //добавление в друзья
    public void addFriend(long userId, long friendToAddId) throws NotFoundException {
        feedStorage.addFriend(userId, friendToAddId);
    }

    //удаление из друзей
    public void deleteFriend(long userId, long friendId) throws NotFoundException {
        feedStorage.deleteFriend(userId, friendId);
    }

    //пользователь ставит лайк фильму
    public void likeFromUser(long filmId, long userId) {
        feedStorage.likeFromUser(filmId, userId);
    }

    //удаляет лайк
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

