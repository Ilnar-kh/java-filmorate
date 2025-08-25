-- Очистка всех таблиц в правильном порядке (с учетом foreign keys)
--DELETE FROM review_likes;
--DELETE FROM reviews;
--DELETE FROM film_likes;
--DELETE FROM user_friends;
--DELETE FROM film_genres;
--DELETE FROM film_directors;
--DELETE FROM users;
--DELETE FROM directors;

-- Очистка справочных таблиц (если нужно)
-- DELETE FROM genres;
-- DELETE FROM mpa;
-- DELETE FROM friendship_statuses;

-- Сброс автоинкремента для основных таблиц
--ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
--ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
--ALTER TABLE directors ALTER COLUMN director_id RESTART WITH 1;
--ALTER TABLE reviews ALTER COLUMN id RESTART WITH 1;

-- Статусы дружбы
MERGE INTO friendship_statuses (code, description) VALUES
  ('PENDING',  'Запрос отправлен'),
  ('CONFIRMED', 'В дружбе'),
  ('DECLINED', 'Отклонено');

-- MPA‑рейтинг
MERGE INTO mpa (id, name) VALUES
  (1, 'G'),
  (2, 'PG'),
  (3, 'PG-13'),
  (4, 'R'),
  (5, 'NC-17');

-- Жанры
MERGE INTO genres (id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

-- Пользователь с id=1
INSERT INTO users (id, email, login, name, birthday)
VALUES (1, 'user1@mail.com', 'user1', 'User One', '1990-01-01');

-- Фильм с id=3
INSERT INTO films (id, name, description, release_date, duration, mpa_id)
VALUES (3, 'The Matrix', 'Classic sci-fi action', '1999-03-31', 136, 1);

-- Добавляем жанры для фильма 3
INSERT INTO film_genres (film_id, genre_id) VALUES (3, 1);
INSERT INTO film_genres (film_id, genre_id) VALUES (3, 2);