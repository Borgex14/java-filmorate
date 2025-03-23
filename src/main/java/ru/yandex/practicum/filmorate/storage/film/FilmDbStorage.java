package ru.yandex.practicum.filmorate.storage.film;

import java.sql.PreparedStatement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        String sqlQueryFilm = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        log.info("Добавление нового фильма: {}", film.getName());

        Mpa mpa = mpaStorage.getNameById(Long.valueOf(film.getMpa().getId()));
        if (mpa == null) {
            throw new NotFoundException("MPA не найдено с ID: " + film.getMpa().getId());
        }

        film.setMpa(mpa);

        List<Genre> genres = film.getGenres() != null ? film.getGenres() : new ArrayList<>();
        List<Genre> selectedGenres = genres.stream()
                .map(genre -> genreStorage.getGenreById(genre.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        film.setGenres(selectedGenres);

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQueryFilm, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        long filmId = Optional.ofNullable(keyHolder.getKey())
                .map(Number::longValue)
                .orElseThrow(() -> new ValidationException("Ошибка добавления фильма в таблицу"));

        filmGenreStorage.addGenresInFilmGenres(film, filmId);

        log.info("Возвращаемый фильм: id={}, name={}, description={}, releaseDate={}, duration={}, mpa={}, genres={}",
                filmId,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getName() : "null",
                film.getGenres() != null ? film.getGenres().stream().map(Genre::getName).collect(Collectors.joining(", ")) : "null");

        log.info("Фильм c id = {} успешно добавлен", filmId);

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

        Mpa mpa = mpaStorage.getNameById(Long.valueOf(film.getMpa().getId()));
        if (mpa == null) {
            throw new NotFoundException("MPA с id = " + film.getMpa().getId() + " не найден");
        }

        List<Genre> genres = Collections.singletonList(genreStorage.getGenreById(film.getId()));

        log.info("Фильм с id = {} успешно обновлён", film.getId());

        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(mpa)
                .genres(genres)
                .build();
    }

    @Override
    public Film getFilm(long id) {
        log.info("Поиск фильма по id = {}", id);

        final String sqlQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.name as mpa_name " +
                "FROM films f " +
                "JOIN rating r ON f.rating_id = r.id " +
                "WHERE f.id = ?";

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
        final String sqlQuery = "SELECT id, name, description, release_date, duration, rating_id FROM films";
        List<Film> films = jdbcTemplate.query(sqlQuery, filmRowMapper::mapRow);
        log.info("Полученные фильмы: {}", films);
        return films;
    }

    @Override
    public void deleteFilm(long id) {
        checkId("films", "id", id);

        String deleteQuery = "DELETE FROM films WHERE id = :id";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", id);

        jdbcOperations.update(deleteQuery, param);
    }

    public void addLike(long filmId, long userId) {
        String addLikeQuery = "INSERT INTO likes (film_id, user_id) VALUES (:film_id, :user_id)";
        log.info("Проверка существования пользователя с ID: {}", userId);
        checkId("users", "id", userId);
        checkId("films", "id", filmId);

        log.info("Добавление лайка для фильма с ID: {} от пользователя с ID: {}", filmId, userId);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("film_id", filmId);
        param.addValue("user_id", userId);

        jdbcOperations.update(addLikeQuery, param, keyHolder, new String[] {"id"});
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

        log.info("Запрос к базе данных для получения популярных фильмов");

        Map<Integer, Integer> resultMap = new LinkedHashMap<>();

        jdbcOperations.query(countQuery, rs -> {
            int amountLikes = rs.getInt("amount_likes");
            int filmId = rs.getInt("film_id");
            resultMap.put(filmId, amountLikes);
        });

        log.info("Количество популярных фильмов: {}", resultMap.size());

        if (resultMap.isEmpty()) {
            log.warn("Нет популярных фильмов, возвращается пустой список");
        }

        return resultMap.keySet().stream()
                .map(this::getFilm)
                .filter(Objects::nonNull) // Фильтруем null значения
                .limit(countInt)
                .toList();
    }

    private void checkId(String tableName, String columnName, long id) {
        String query = String.format("SELECT COUNT(*) FROM %s WHERE %s = :id", tableName, columnName);
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", id);

        int count = jdbcOperations.queryForObject(query, param, Integer.class);

        if (count == 0) {
            throw new ValidationException("Объект не найден: " + tableName + " с id " + id);
        }
    }

    private void checkMpa(Mpa mpa) {
        String checkMpaQuery = "SELECT EXISTS (SELECT 1 FROM rating WHERE id = :id)";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", mpa.getId());

        if (jdbcOperations.queryForObject(checkMpaQuery, param, Long.class) == 0) {
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
}

