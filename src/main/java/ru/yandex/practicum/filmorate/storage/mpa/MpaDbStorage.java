package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Repository
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private MpaRating mapRowToMpaRating(ResultSet resultSet, int rowIndex) throws SQLException {
        MpaRating rating = new MpaRating();
        rating.setId(resultSet.getInt("id"));
        rating.setName(resultSet.getString("name"));
        return rating;
    }

    @Override
    public List<MpaRating> findAll() {
        String sql = "SELECT id, name FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToMpaRating);
    }

    @Override
    public MpaRating findById(int id) {
        String sql = "SELECT id, name FROM mpa WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToMpaRating, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA с id = " + id + " не найден");
        }
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
