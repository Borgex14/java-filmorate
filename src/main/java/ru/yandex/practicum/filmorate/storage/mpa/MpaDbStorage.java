package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Objects;
import java.util.Optional;



@Slf4j
@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
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
        String sqlQuery = "SELECT * from rating";
        return jdbcTemplate.query(sqlQuery, mpaRowMapper::mapRow);
    }

    @Override
    public Mpa getNameById(Long id) {
        log.info("Поиск MPA по id: {}", id);
        String sqlQuery = "SELECT * " +
                "FROM rating where id = ?";

        Optional<Mpa> resultMpa;

        try {
            resultMpa = Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery,
                    mpaRowMapper::mapRow, id));
        } catch (EmptyResultDataAccessException e) {
            resultMpa = Optional.empty();
        }

        if (resultMpa.isPresent()) {
            return resultMpa.get();

        } else {
            log.error("Mpa с id = {} не найден", id);
            throw new NotFoundException("Mpa с id = " + id + " не найден");
        }
    }

    @Override
    public Integer getCountById(Film film) {
        log.info("Проверка существования mpa_id = {} в таблице rating", film.getMpa().getId());
        Integer count;
        final String sqlQueryMpa = "SELECT COUNT(*) " +
                "FROM rating WHERE id = ?";

        try {
            count = jdbcTemplate.queryForObject(sqlQueryMpa, Integer.class, film.getMpa().getId());
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException("MPA id не существуют");
        }

        if (Objects.isNull(count) || count == 0) {
            throw new ValidationException("MPA id не существует");
        }

        return count;
    }
}
