package ru.yandex.practicum.filmorate.storage.genre;

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
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final NamedParameterJdbcOperations jdbcOperations;
    private  final GenreRowMapper genreRowMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getGenreById(long id) {

        String getGenreQuery = "SELECT id, name AS cnt FROM genres WHERE id = :id";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", id);

        try {
            return jdbcOperations.queryForObject(getGenreQuery, param, genreRowMapper);
        } catch (DataAccessException e) {
            throw new NotFoundException("Жанр с id " + id + " не существует.");
        }
    }

    public Collection<Genre> getAllGenres() {
        String sql = "SELECT id, name FROM genres LIMIT 6";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            return new Genre(id, name);
        });
    }

    public Collection<Long> findIds() {
        String sqlQuery = "SELECT id from genres";
        return jdbcTemplate.queryForList(sqlQuery, Long.class);
    }

    @Override
    public Genre getNameById(long id) {
        log.info("Поиск жанра по id: {}", id);
        String sqlQuery = "SELECT * " +
                "FROM genres where id = ?";

        Optional<Genre> resultGenre;

        try {
            resultGenre = Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery,
                    genreRowMapper::mapRow, id));
        } catch (EmptyResultDataAccessException e) {
            resultGenre = Optional.empty();
        }

        if (resultGenre.isPresent()) {
            return resultGenre.get();

        } else {
            log.error("Жанр с id = {} не найден", id);
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }

    public Collection<Genre> getExistGenres(Film film) {
        List<Long> genreIds = findIds().stream().toList();
        List<Genre> filmGenres = film.getGenres();
        List<Genre> resultGenres = new ArrayList<>();

        if (filmGenres != null) {
            for (Genre genre : filmGenres) {
                if (genre != null) {
                    if (genreIds.contains(genre.getId())) {
                        resultGenres.add(genre);
                    } else {
                        throw new ValidationException("Указанный жанр не существует");
                    }
                } else {
                    throw new ValidationException("Найден null жанр в списке");
                }
            }
        } else {
            throw new ValidationException("Список жанров фильма пустой");
        }
        return resultGenres;
    }
}
