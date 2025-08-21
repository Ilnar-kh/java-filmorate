package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review create(Review review);

    Review findById(Long reviewId);

    Review update(Review review);

    int delete(Long id);

    List<Review> getReviewsByFilmId(Long id, int count);

    void putRate(Long id, Long userId, Boolean isUseful);

    void deleteRate(Long id, Long userId, Boolean isUseful);

}
