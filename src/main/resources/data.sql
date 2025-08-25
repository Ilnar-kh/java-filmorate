-- Очищаем пользовательские данные ПЕРЕД вставкой справочников
DELETE FROM film_likes;
DELETE FROM user_friends;
DELETE FROM film_genres;
DELETE FROM films;
DELETE FROM users;
DELETE FROM FEEDS;

-- Сбрасываем автоинкремент для таблиц
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE FEEDS ALTER COLUMN ENTITYID RESTART WITH 1;

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

