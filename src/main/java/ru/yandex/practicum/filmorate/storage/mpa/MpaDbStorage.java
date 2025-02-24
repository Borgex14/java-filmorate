package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Primary
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final NamedParameterJdbcOperations jdbcOperations;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Mpa getRatingById(int id) {
        String getRatingQuery = "SELECT name FROM rating WHERE id = :id";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", id);

        try {
            return new Mpa(id, jdbcOperations.queryForObject(getRatingQuery, param, String.class));
        } catch (DataAccessException e) {
            throw new NotFoundException("Рейтинга с id " + id + " не существует.");
        }

    }

    @Override
    public List<Mpa> getAllRatings() {
        String getAllRatingsQuery = "SELECT id, name  FROM rating";
        return jdbcOperations.query(getAllRatingsQuery, mpaRowMapper);
    }
}
