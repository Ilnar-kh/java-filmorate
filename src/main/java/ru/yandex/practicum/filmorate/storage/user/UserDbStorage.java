package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    @Override
    public User create(User user) {
        final String sql = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        if (findById(user.getId()) == null) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        final String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbc.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public List<User> findAll() {
        final String sql = "SELECT * FROM users";
        return jdbc.query(sql, this::mapRowToUser);
    }

    @Override
    public User findById(Long userId) {
        final String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbc.query(sql, this::mapRowToUser, userId);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return users.get(0);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (findById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (findById(friendId) == null) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        final String sql = "INSERT INTO user_friends(requester_id, addressee_id, status_code) VALUES (?, ?, 'CONFIRMED')";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (findById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (findById(friendId) == null) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        final String sql = "DELETE FROM user_friends WHERE requester_id = ? AND addressee_id = ?";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        if (findById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        final String sql = "SELECT u.* FROM users u " +
                "JOIN user_friends uf ON uf.addressee_id = u.id " +
                "WHERE uf.requester_id = ?";
        return jdbc.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        if (findById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (findById(otherUserId) == null) {
            throw new NotFoundException("Пользователь с id=" + otherUserId + " не найден");
        }

        final String sql =
                "SELECT u.* FROM users u WHERE u.id IN (" +
                        "SELECT uf1.addressee_id FROM user_friends uf1 " +
                        "JOIN user_friends uf2 ON uf1.addressee_id = uf2.addressee_id " +
                        "WHERE uf1.requester_id = ? AND uf2.requester_id = ?)";
        return jdbc.query(sql, this::mapRowToUser, userId, otherUserId);
    }

    public int removeById(Long userId) {
        return jdbc.update("DELETE FROM users WHERE id = ?", userId);
    }
}
