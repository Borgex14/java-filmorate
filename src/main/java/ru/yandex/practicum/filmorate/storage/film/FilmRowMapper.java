package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static ru.yandex.practicum.filmorate.exception.GlobalExceptionHandler.log;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.info("Старт метода Film mapRowToFilm(ResultSet rs, int rowNum)");

        String releaseDateString = null;
        try {
            releaseDateString = rs.getString("release_date");
            log.info("Полученная строка даты: {}", releaseDateString);

            LocalDate releaseDate = LocalDate.parse(releaseDateString);

            return Film.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(releaseDate)
                    .duration(rs.getInt("duration"))
                    .build();
        } catch (DateTimeParseException e) {
            log.error("Ошибка при парсинге даты: {}", e.getMessage());
            throw new SQLException("Неверный формат даты: " + releaseDateString, e);
        }
    }
}