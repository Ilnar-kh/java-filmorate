-- Очистка всех таблиц в правильном порядке (с учетом foreign keys)
DELETE FROM review_likes;
DELETE FROM reviews;
DELETE FROM feeds;
DELETE FROM film_likes;
DELETE FROM user_friends;
DELETE FROM film_genres;
DELETE FROM film_directors;
DELETE FROM films;
DELETE FROM users;
DELETE FROM directors;

-- Очистка справочных таблиц (если нужно)
--DELETE FROM genres;
--DELETE FROM mpa;
--DELETE FROM friendship_statuses;

-- Сброс автоинкремента (зависит от СУБД, пример для PostgreSQL и H2)
-- Для PostgreSQL замените на ALTER SEQUENCE ... RESTART WITH 1, если используются SEQUENCE

-- Сброс автоинкремента для основных таблиц H2
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE directors ALTER COLUMN director_id RESTART WITH 1;
ALTER TABLE reviews ALTER COLUMN id RESTART WITH 1;
ALTER TABLE feeds ALTER COLUMN eventId RESTART WITH 1;

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
