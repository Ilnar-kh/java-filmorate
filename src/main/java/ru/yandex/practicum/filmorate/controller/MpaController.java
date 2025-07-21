package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaStorage mpaStorage;

    @GetMapping
    public List<MpaRating> getAllMpaRatings() {
        log.info("GET   /mpa — запрос списка всех MPA‑рейтингов");
        return mpaStorage.findAll();
    }

    @GetMapping("/{ratingId}")
    public MpaRating getMpaRatingById(
            @PathVariable("ratingId") Integer ratingId
    ) {
        log.info("GET   /mpa/{} — запрос MPA‑рейтинга по id", ratingId);
        return mpaStorage.findById(ratingId);
    }
}
