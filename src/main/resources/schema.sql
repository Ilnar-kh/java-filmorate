-- Справочник MPA‑рейтингов
CREATE TABLE IF NOT EXISTS mpa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    release_date DATE    NOT NULL,
    duration     INT     NOT NULL,
    mpa_id   VARCHAR(10),
    FOREIGN KEY (mpa_id) REFERENCES mpa(id)
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT  PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    login      VARCHAR(100) NOT NULL,
    name       VARCHAR(150),
    birthday   DATE
);

-- Таблица лайков фильмов
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Справочник статусов дружбы
CREATE TABLE IF NOT EXISTS friendship_statuses (
    code        VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255)
);

-- Связь «пользователь → пользователь» с указанием статуса
CREATE TABLE IF NOT EXISTS user_friends (
    requester_id INT       NOT NULL,
    addressee_id INT       NOT NULL,
    status_code  VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (requester_id, addressee_id),
    FOREIGN KEY (requester_id)   REFERENCES users(id),
    FOREIGN KEY (addressee_id)   REFERENCES users(id),
    FOREIGN KEY (status_code)    REFERENCES friendship_statuses(code)
);

-- Справочник жанров
CREATE TABLE IF NOT EXISTS genres (
    id   INT          PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Связь «фильм ↔ жанр»
CREATE TABLE IF NOT EXISTS film_genres (
    film_id  INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id)  REFERENCES films(id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_films_name ON films(name);
CREATE INDEX IF NOT EXISTS idx_genres_name ON genres(name);
