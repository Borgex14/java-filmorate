package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
public class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private MpaStorage mpaStorage;

    @Autowired
    private GenreStorage genreStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("INSERT INTO rating (name) VALUES ('G')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Action')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Comedy')");
    }

    @Test
    public void testAddAndGetFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("A film for testing")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .mpa(mpaStorage.getRatingById(1))
                .genres(List.of(genreStorage.getGenreById(1L)))
                .build();

        Film addedFilm = filmDbStorage.addFilm(film);
        assertThat(addedFilm).isNotNull();
        assertThat(addedFilm.getId()).isGreaterThan(0);

        List<Film> retrievedFilms = filmDbStorage.getFilms(List.of(addedFilm.getId()));

        assertThat(retrievedFilms)
                .isNotEmpty()
                .hasSize(1)
                .first()
                .satisfies(retrieved -> {
                    assertThat(retrieved).hasFieldOrPropertyWithValue("name", "Test Film");
                    assertThat(retrieved).hasFieldOrPropertyWithValue("description", "A film for testing");
                    assertThat(retrieved.getMpa()).isEqualTo(mpaStorage.getRatingById(1));
                    assertThat(retrieved.getGenres()).containsExactlyInAnyOrderElementsOf(List.of(genreStorage.getGenreById(1L)));
                });
    }

    @Test
    public void testUpdateFilm() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .mpa(mpaStorage.getRatingById(1))
                .genres(List.of(genreStorage.getGenreById(1L)))
                .build();

        Film addedFilm = filmDbStorage.addFilm(film);

        addedFilm.setName("Updated Film");
        addedFilm.setDescription("Updated description");

        Film updatedFilm = filmDbStorage.updateFilm(addedFilm);

        assertThat(updatedFilm).hasFieldOrPropertyWithValue("name", "Updated Film");
        assertThat(updatedFilm).hasFieldOrPropertyWithValue("description", "Updated description");
    }
}