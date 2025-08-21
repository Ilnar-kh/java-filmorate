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

    public Review create(Review review) {
        userStorage.findById(review.getUserId());
        filmStorage.findById(review.getFilmId());

        return reviewStorage.create(review);
    }

    public Review findById(Long id) {
        try {
            return reviewStorage.findById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
    }

    public Review update(Review review) {
        findById(review.getId());
        return reviewStorage.update(review);
    }

    public void delete(Long id) {
        int rows = reviewStorage.delete(id);

        if (rows == 0) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
    }

    public List<Review> getReviewsByFilmId(Long id, int count) {
        return reviewStorage.getReviewsByFilmId(id, count);
    }

    public void putRate(Long id, Long userId, Boolean isUseful) {
        userStorage.findById(userId);
        findById(id);
        reviewStorage.putRate(id, userId, isUseful);
    }

    public void deleteRate(Long id, Long userId, Boolean isUseful) {
        userStorage.findById(userId);
        findById(id);
        reviewStorage.deleteRate(id, userId, isUseful);
    }
}
