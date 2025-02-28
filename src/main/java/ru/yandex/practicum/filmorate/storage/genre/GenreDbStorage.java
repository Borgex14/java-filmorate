package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final NamedParameterJdbcOperations jdbcOperations;
    private  final GenreRowMapper genreRowMapper;

    @Override
    public Genre getGenreById(int id) {

        String getGenreQuery = "SELECT id, name AS cnt FROM genre WHERE id = :id";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", id);

        try {
            return jdbcOperations.queryForObject(getGenreQuery, param, genreRowMapper);
        } catch (DataAccessException e) {
            throw new NotFoundException("Жанр с id " + id + " не существует.");
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        String getAllGenresQuery = "SELECT id, name  FROM genre";
        return jdbcOperations.query(getAllGenresQuery, genreRowMapper);
    }
}
