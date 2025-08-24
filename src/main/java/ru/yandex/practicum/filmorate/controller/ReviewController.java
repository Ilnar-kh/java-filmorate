package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final FeedService feedService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        Review rev = reviewService.create(review);
        feedService.addReview(rev);
        return rev;
    }

    @GetMapping("/{id}")
    public Review findById(@PathVariable Long id) {
        return reviewService.findById(id);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        Review rev = reviewService.update(review);
        feedService.updateReview(rev);
        return rev;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive Long id) {
        feedService.deleteReview(id, reviewService.findById(id).getUserId());
        reviewService.delete(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(value = "filmId", required = false) @Positive Long filmId,
            @RequestParam(value = "count", defaultValue = "10") @Positive Integer count
    ) {
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.putRate(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void putDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.putRate(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteRate(id, userId, true);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteRate(id, userId, false);
    }
}
