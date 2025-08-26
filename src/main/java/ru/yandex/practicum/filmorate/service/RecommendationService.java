package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public List<Film> getRecommendations(Long userId) {
        // проверяем существование пользователя
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));

        // лайки пользователя
        Set<Long> userLikes = userStorage.getUserLikedFilms(userId);

        Long similarUserId = null;
        int maxCommon = 0;

        for (User other : userStorage.findAll()) {
            if (other.getId().equals(userId)) continue;

            Set<Long> otherLikes = userStorage.getUserLikedFilms(other.getId());
            Set<Long> common = new HashSet<>(userLikes);
            common.retainAll(otherLikes);

            if (common.size() > maxCommon) {
                maxCommon = common.size();
                similarUserId = other.getId();
            }
        }

        if (similarUserId == null) {
            return Collections.emptyList();
        }

        // фильмы похожего, которых нет у userId
        Set<Long> similarLikes = new LinkedHashSet<>(userStorage.getUserLikedFilms(similarUserId));
        similarLikes.removeAll(userLikes);

        return similarLikes.stream()
                .map(id -> filmStorage.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
