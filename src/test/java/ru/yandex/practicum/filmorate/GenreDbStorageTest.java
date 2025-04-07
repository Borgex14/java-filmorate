package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
public class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Action')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Comedy')");
    }

    @Test
    public void testGetExistGenres() {
        Film film = new Film();
        film.setGenres(List.of(new Genre(1, "Action"), new Genre(2, "Comedy")));

        Collection<Genre> existingGenres = genreDbStorage.getExistGenres(film);
        assertThat(existingGenres).hasSize(2);
    }

    @Test
    public void testGetExistGenresWithInvalidGenre() {
        Film film = new Film();
        film.setGenres(List.of(new Genre(3, "Nonexistent")));

        try {
            genreDbStorage.getExistGenres(film);
        } catch (ValidationException e) {
            assertThat(e.getMessage()).isEqualTo("Указанный жанр не существует");
        }
    }

    @Test
    public void testGetExistGenresWithEmptyList() {
        Film film = new Film();
        film.setGenres(null);

        try {
            genreDbStorage.getExistGenres(film);
        } catch (ValidationException e) {
            assertThat(e.getMessage()).isEqualTo("Список жанров фильма пустой");
        }
    }
}