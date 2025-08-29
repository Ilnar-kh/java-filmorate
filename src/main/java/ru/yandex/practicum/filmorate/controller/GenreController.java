package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreStorage genreStorage;

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("GET   /genres — запрос списка всех жанров");
        return genreStorage.findAll();
    }

    @GetMapping("/{genreId}")
    public Genre getGenreById(
            @PathVariable("genreId") Integer genreId
    ) {
        log.info("GET   /genres/{} — запрос жанра по id", genreId);
        return genreStorage.findById(genreId);
    }
}
