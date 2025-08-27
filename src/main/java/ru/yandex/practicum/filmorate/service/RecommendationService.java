package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class RecommendationService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public List<Film> getRecommendations(Long userId) {
        // 1. Проверяем, что пользователь существует
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));

        // 2. Забираем все лайки всех пользователей одним запросом
        Map<Long, Set<Long>> userLikesMap = userStorage.getAllUserLikedFilms();

        // 3. Лайки текущего пользователя
        Set<Long> userLikes = userLikesMap.getOrDefault(userId, Collections.emptySet());

        Long similarUserId = null;
        int maxCommon = 0;

        // 4. Находим пользователя с максимальным пересечением лайков
        for (Map.Entry<Long, Set<Long>> entry : userLikesMap.entrySet()) {
            Long otherId = entry.getKey();
            if (otherId.equals(userId)) continue;

            Set<Long> otherLikes = entry.getValue();
            Set<Long> common = new HashSet<>(userLikes);
            common.retainAll(otherLikes);

            if (common.size() > maxCommon) {
                maxCommon = common.size();
                similarUserId = otherId;
            }
        }

        // 5. Если похожего пользователя нет — рекомендаций нет
        if (similarUserId == null) {
            return Collections.emptyList();
        }

        // 6. Фильмы похожего пользователя, которых нет у userId
        Set<Long> recommendedIds = new HashSet<>(userLikesMap.get(similarUserId));
        recommendedIds.removeAll(userLikes);

        if (recommendedIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 7. Загружаем все фильмы одним запросом
        return filmStorage.findFilmsByIds(recommendedIds);
    }
}
