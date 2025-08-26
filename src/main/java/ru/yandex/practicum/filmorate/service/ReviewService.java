package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedService feedService;

    public Review create(Review review) {
        userStorage.findById(review.getUserId())
                .orElseThrow(() -> new NotFoundException("User " + review.getUserId() + " not found"));
        filmStorage.findById(review.getFilmId())
                .orElseThrow(() -> new NotFoundException("Film " + review.getFilmId() + " not found"));
        Review rev = reviewStorage.create(review);
        feedService.addReview(rev);
        return rev;
    }

    public Review findById(Long id) {
        try {
            return reviewStorage.findById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
    }

    public Review update(Review review) {
        // 404 если нет
        findById(review.getId());
        Review rev = reviewStorage.update(review);
        feedService.updateReview(rev);
        return rev;
    }

    public void delete(Long id) {
        // достаём, чтобы гарантировать 404 и знать userId для фида
        Review existing = findById(id);
        feedService.deleteReview(id, existing.getUserId());
        int rows = reviewStorage.delete(id);
        if (rows == 0) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
    }

    public List<Review> getReviewsByFilmId(Long id, int count) {
        // по желанию можно проверить существование фильма
        return reviewStorage.getReviewsByFilmId(id, count);
    }

    public void putRate(Long id, Long userId, Boolean isUseful) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        findById(id);
        reviewStorage.putRate(id, userId, isUseful);
    }

    public void deleteRate(Long id, Long userId, Boolean isUseful) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        findById(id);
        reviewStorage.deleteRate(id, userId, isUseful);
    }
}
