package ru.yandex.practicum.filmorate.storage.film;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.filmGenre.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;


@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcOperations jdbcOperations;
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreStorage genreStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final MpaStorage mpaStorage;

    @Override
    public Film addFilm(Film film) {
        checkMpa(film.getMpa());

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        log.info("Добавление нового фильма: {}", film.getName());

        String sqlQueryFilm = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        Mpa mpa = mpaStorage.getNameById(Long.valueOf(film.getMpa().getId()));
        if (mpa == null) {
            throw new NotFoundException("MPA не найдено с ID: " + film.getMpa().getId());
        }
        film.setMpa(mpa);

        List<Genre> genres = film.getGenres() != null ? film.getGenres() : new ArrayList<>();
        List<Genre> selectedGenres = genres.stream()
                .map(genre -> genreStorage.getGenreById(genre.getId()))
                .collect(Collectors.toList());

        film.setGenres(selectedGenres);

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQueryFilm, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay())); // Убедитесь, что формат правильный
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        long filmId = Optional.ofNullable(keyHolder.getKey())
                .map(Number::longValue)
                .orElseThrow(() -> new ValidationException("Ошибка добавления фильма в таблицу"));

        filmGenreStorage.addGenresInFilmGenres(film, filmId);

        log.info("Фильм c id = {} успешно добавлен", filmId);
        log.info("Film MPA: {}", film.getMpa().getName());
        log.info("Film Genres: {}", film.getGenres());

        return Film.builder()
                .id(filmId)
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .genres(film.getGenres())
                .build();
    }


    @Override
    public Film updateFilm(Film film) {
        checkMpa(film.getMpa());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        log.info("Обновление данных фильма с id = {}", film.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String sqlQuery = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, duration = ? " +
                "where id = ?";

        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setString(3, film.getReleaseDate().toString());
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getId());
            return stmt;
        }, keyHolder);

        if (rows == 0) {
            log.error("Фильм с id = {} не найден для обновления", film.getId());
            throw new NotFoundException("Ошибка обновления: фильм с id = " + film.getId() + " не найден");
        }

        log.info("Фильм с id = {} успешно обновлён", film.getId());

        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa()) // добавим MPA для полноты обновленного объекта
                .build();
    }


    @Override
    public Film getFilm(long id) {
        log.info("Поиск фильма по id = {}", id);

        final String sqlQuery = "SELECT id, name, description, releaseDate, duration, mpa_id " +
                "FROM films WHERE id = ?";

        Optional<Film> resultFilm = Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery,
                filmRowMapper::mapRow, id));

        if (resultFilm.isPresent()) {
            return resultFilm.get();

        } else {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        final String sqlQuery = "SELECT id, name, description, releaseDate, duration, mpa_id FROM films";
        return jdbcTemplate.query(sqlQuery, filmRowMapper::mapRow);
    }

    @Override
    public void deleteFilm(long id) {
        checkId("films", "film_id", id); // Проверяем, существует ли фильм с данным id

        String deleteQuery = "DELETE FROM films WHERE id = :filmId"; // SQL-запрос для удаления фильма

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("filmId", id); // Добавляем параметр id в запрос

        jdbcOperations.update(deleteQuery, param); // Выполняем обновление в базе данных
    }

    public void addLike(long filmId, long userId) {
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
        checkId("films", "id", filmId);
        checkId("users", "id", userId);
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
        String checkMpaQuery = "SELECT EXISTS (SELECT 1 FROM rating WHERE id = :id)";
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

