package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecommendationService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public List<Film> getRecommendations(Long userId) {
        // Фильмы, которые лайкнул текущий пользователь
        Set<Long> userLikes = userStorage.getUserLikedFilms(userId);

        Long similarUserId = null;
        int maxCommon = 0;

        // Ищем пользователя с максимальным пересечением по лайкам
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

        // Рекомендации = фильмы, которые лайкнул похожий, но не лайкнул userId
        Set<Long> similarLikes = new HashSet<>(userStorage.getUserLikedFilms(similarUserId));
        similarLikes.removeAll(userLikes);

        return similarLikes.stream()
                .map(filmStorage::findById)
                .toList();
    }
}
