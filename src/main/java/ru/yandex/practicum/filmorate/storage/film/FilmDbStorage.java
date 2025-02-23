package ru.yandex.practicum.filmorate.storage.film;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;

@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {


    private final NamedParameterJdbcOperations jdbcOperations;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film addFilm(Film film) {
        checkMpa(film.getMpa());
        checkGenre(film.getGenres());

        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (:name, :description, :releaseDate, :duration, :ratingId)";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("name", film.getName());
        parameterSource.addValue("description", film.getDescription());
        parameterSource.addValue("releaseDate", Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
        parameterSource.addValue("duration", film.getDuration());
        parameterSource.addValue("ratingId", film.getMpa().getId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcOperations.update(sqlQuery, parameterSource, keyHolder, new String[] {"film_id"});

        film.setId(keyHolder.getKey().longValue());

        String sqlQuery1 = "INSERT INTO film_genres_list (genre_id, film_id) VALUES (:genreId, :filmId)";

        List<Map<String, Object>> genreBatchValues = new ArrayList<>();

        List<Genre> genres = film.getGenres();

        for (Genre genre : genres) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("genreId", genre.getId());
            paramMap.put("filmId", film.getId());
            genreBatchValues.add(paramMap);
        }

        jdbcOperations.batchUpdate(sqlQuery1, genreBatchValues.toArray(new Map[0]));

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        checkMpa(film.getMpa());
        checkGenre(film.getGenres());
        checkId("films", "film_id", film.getId());

        String updateQuery = "UPDATE films SET name = :name, description = :description, release_date = :releaseDate, " +
                "duration = :duration, rating_id = :mpaId WHERE film_id = :id";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("name", film.getName());
        param.addValue("description", film.getDescription());
        param.addValue("releaseDate", new Timestamp(film.getReleaseDate().atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant().getLong(ChronoField.INSTANT_SECONDS)));
        param.addValue("duration", film.getDuration());
        param.addValue("mpaId", film.getMpa().getId());
        param.addValue("id", film.getId());

        jdbcOperations.update(updateQuery, param);

        String deleteFilmGenresQuery = "DELETE FROM film_genres_list WHERE film_id = :id";

        jdbcOperations.update(deleteFilmGenresQuery, param);

        String addGenresQuery = "INSERT INTO film_genres_list (film_id, genre_id) VALUES (:filmId, :genreId)";

        List<Map<String, Object>> genreBatchValues = new ArrayList<>();

        List<Genre> genres = film.getGenres();

        for (Genre genre : genres) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("genreId", genre.getId());
            paramMap.put("filmId", film.getId());
            genreBatchValues.add(paramMap);
        }

        jdbcOperations.batchUpdate(addGenresQuery, genreBatchValues.toArray(new Map[0]));

        return film;
    }

    @Override
    public Film getFilm(long id) {
        String sqlQuery1 = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_name" +
                " FROM films AS f INNER JOIN rating AS r ON f.rating_id = r.id WHERE film_id = :id";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", id);

        Film film = jdbcOperations.queryForObject(sqlQuery1, param, filmRowMapper);

        String sqlQuery2 = "SELECT DISTINCT fl.genre_id, g.genre_name FROM film_genres_list AS fl INNER JOIN genre AS g " +
                "ON fl.genre_id = g.genre_id WHERE film_id = :id";

        List<Genre> filmGenres = jdbcOperations.query(sqlQuery2, param, new GenreRowMapper());

        film.setGenres(filmGenres);

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        String query1 = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_name" +
                " FROM films AS f INNER JOIN rating AS r ON f.rating_id = r.rating_id GROUP BY f.film_id";

        List<Film> filmsList = jdbcOperations.query(query1,filmRowMapper);

        String query2 = "SELECT DISTINCT f.film_id, f.genre_id, g.genre_name FROM film_genres_list AS f INNER JOIN genres AS g ON f.genre_id = g.genre_id ";

        Map<Integer, List<Genre>> resultMap = new LinkedHashMap<>();


        jdbcOperations.query(query2, rs -> {
            int genreId = rs.getInt("genre_id");
            int filmId = rs.getInt("film_id");
            String genreName = rs.getString("genre_name");
            resultMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(Genre.builder().id(genreId).name(genreName).build());
        });

        for (Film film : filmsList) {
            film.setGenres(resultMap.get(film.getId()));
        }

        return filmsList;
    }

    @Override
    public void deleteFilm(long id) {
        checkId("films", "film_id", id); // Проверяем, существует ли фильм с данным id

        String deleteQuery = "DELETE FROM films WHERE id = :filmId"; // SQL-запрос для удаления фильма

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("filmId", id); // Добавляем параметр id в запрос

        jdbcOperations.update(deleteQuery, param); // Выполняем обновление в базе данных
    }

    public void addLike(int filmId, int userId) {
        String addLikeQuery = "INSERT INTO likes (film_id, user_id) VALUES (:filmId, :userId)";
        checkId("films", "film_id", filmId);
        checkId("users", "user_id", userId);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("filmId", filmId);
        param.addValue("userId", userId);

        jdbcOperations.update(addLikeQuery, param, keyHolder, new String[] {"like_id"});
    }

    @Override
    public void removeLike(long filmId, long userId) {
        checkId("films", "film_id", filmId);
        checkId("users", "user_id", userId);
        checkLike(filmId, userId);

        String deleteQuery = "DELETE FROM likes WHERE film_id = :filmId AND user_id = :userId";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("filmId", filmId);
        param.addValue("userId", userId);

        jdbcOperations.update(deleteQuery, param);
    }

    public List<Film> getTopFilms(String count) {

        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (Exception e) {
            throw  new ValidationException("Передано не число, а " + count);
        }
        if (countInt <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше нуля");
        }

        String countQuery = "SELECT COUNT(film_id) AS amount_likes, film_id FROM likes " +
                "GROUP BY film_id ORDER BY amount_likes DESC";

        Map<Integer, Integer> resultMap = new LinkedHashMap<>();

        jdbcOperations.query(countQuery, rs -> {
            int amountLikes = rs.getInt("amount_likes");
            int filmId = rs.getInt("film_id");
            resultMap.put(filmId, amountLikes);
        });

        return resultMap.keySet().stream()
                .map(this::getFilm)
                .limit(Long.parseLong(count))
                .toList();
    }

    private void checkId(String tableName, String columnName, long id) {
        String query = String.format("SELECT EXISTS(SELECT 1 FROM %s WHERE %s = :id)", tableName, columnName);
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("id", id);
        try {
            if (jdbcOperations.queryForObject(query, parameterSource, Integer.class) == 0) {
                throw new NotFoundException("Пользователь с id " + id + " не найден.");
            }
        } catch (DataAccessException e) {
            e.getCause().printStackTrace();
        }
    }

    private void checkMpa(Mpa mpa) {
        String checkMpaQuery = "SELECT EXISTS (SELECT 1 FROM rating WHERE rating_id = :id)";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", mpa.getId());

        if (jdbcOperations.queryForObject(checkMpaQuery, param, Integer.class) == 0) {
            throw new NotFoundException("Рейтинг с id " + mpa.getId() + " не найден.");
        }
    }

    private void checkLike(long filmId, long userId) {
        String checkLikeQuery = String.format("SELECT EXISTS (SELECT 1 FROM likes) WHERE %s = :filmId AND %s = :userId",
                filmId, userId);
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("filmId", filmId);
        param.addValue("userId", userId);

        if (jdbcOperations.queryForObject(checkLikeQuery, param, Integer.class) == 0) {
            throw new NotFoundException("Лайк от пользователя с id " + userId + " фильму с id " + filmId + " не найден.");
        }
    }

    private void checkGenre(List<Genre> genresList) {
        String checkMpaQuery = "SELECT id FROM genres";
        List<Integer> genresIdList = jdbcOperations.queryForList(checkMpaQuery, new HashMap<>(), Integer.class);

        for (Genre genre : genresList) {
            if (!genresIdList.contains(genre.getId())) {
                throw new NotFoundException("Жанра с id " + genre.getId() + " не найден.");
            }
        }
    }
}

