package ru.yandex.practicum.filmorate.storage.filmGenre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmGenreDbStorage implements FilmGenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final GenreRowMapper genreRowMapper;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Map<Long, Set<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        final String sqlQuery = "SELECT film_id, genre_id FROM film_genres WHERE film_id IN (:filmIds)";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("filmIds", filmIds);

        List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sqlQuery, parameters);

        Map<Long, Set<Genre>> genresMap = new HashMap<>();

        for (Map<String, Object> row : results) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Long genreId = ((Number) row.get("genre_id")).longValue();

            // Получаем или создаем множество жанров для данного фильма
            Set<Genre> genres = genresMap.computeIfAbsent(filmId, k -> new HashSet<>());

            // Здесь вы должны создать объект Genre на основе genreId
            Genre genre = new Genre(genreId); // Предполагается, что у вас есть конструктор Genre, который принимает genreId
            genres.add(genre);
        }

        return genresMap;
    }

    @Override
    public Collection<Genre> findAll() {
        final String sqlQuery = "SELECT * FROM film_genres";
        return jdbcTemplate.query(sqlQuery, genreRowMapper::mapRow);
    }

    @Override
    public void addGenresInFilmGenres(Film film, Long id) {
        List<Genre> resultGenres = (List<Genre>) genreStorage.getExistGenres(film);

        if (resultGenres.isEmpty()) {
            log.warn("Нет жанров для добавления в film_genres");
            return;
        }

        final String sqlQueryFilmGenres = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";

        log.info("Вставка жанров для фильма с ID: {}", id);

        jdbcTemplate.batchUpdate(sqlQueryFilmGenres, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setLong(1, id);
                preparedStatement.setLong(2, resultGenres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return resultGenres.size();
            }
        });
    }

    public List<Genre> getListGenreFromDbGenres(Long filmId) {
        List<Genre> result = new ArrayList<>();

        String filmGenresQuery = "SELECT genre_id, " +
                "FROM film_genres " +
                "WHERE film_id = ? ";

        List<Long> genreIds = jdbcTemplate.queryForList(filmGenresQuery, Long.class, filmId);
        List<Genre> genres = genreStorage.getAllGenres().stream().toList();

        for (Genre genre : genres) {
            if (genreIds.contains(genre.getId())) {
                result.add(genre);
            }
        }

        return result;
    }

    @Override
    public Collection<Genre> findByFilmId(Long id) {
        final String sqlQuery = "SELECT genre_id FROM film_genres WHERE film_id = ? ";
        return jdbcTemplate.query(sqlQuery, genreRowMapper::mapRow, id);
    }
}
