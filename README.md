# java-filmorate
Template repository for Filmorate project.

Ссылка на ER-диаграмму
https://disk.yandex.ru/i/yXMJB9fRaTJkiQ

Пояснение к схеме базы данных
=

Таблица users
=
Содержит данные пользователей:
id — уникальный идентификатор
email, login, name — контактные и идентификационные данные
birthday — дата рождения

Таблица user_friends
=
Представляет отношения дружбы между пользователями:
requester_id — пользователь, отправивший запрос
addressee_id — пользователь, получивший запрос

status_code — статус дружбы (например, CONFIRMED, UNCONFIRMED)

created_at — дата создания связи

Таблица friendship_statuses
=
Справочник возможных статусов дружбы:
UNCONFIRMED — неподтверждено
CONFIRMED — подтверждено

Таблица films
=
Содержит информацию о фильмах:
id, name, description, release_date, duration
mpa_rating — внешний ключ на таблицу рейтингов MPA

Таблица mpa_ratings
=
Справочник возрастных рейтингов:
G, PG, PG-13, R, NC-17

Таблица genres
=
Справочник жанров фильмов:
id, name

Таблица film_genres
=
Связующая таблица между фильмами и жанрами (многие-ко-многим):
film_id, genre_id

Пример запроса: 
=
Получить все фильмы с жанрами и рейтингами
=
SELECT f.id, f.name, f.description, f.release_date, f.duration,
       r.code AS mpa_rating,
       g.name AS genre
FROM films f
LEFT JOIN mpa_ratings r ON f.mpa_rating = r.code
LEFT JOIN film_genres fg ON fg.film_id = f.id
LEFT JOIN genres g ON fg.genre_id = g.id;
