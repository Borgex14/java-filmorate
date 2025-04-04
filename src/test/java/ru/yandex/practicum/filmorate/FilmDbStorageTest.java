package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
public class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film testFilm;

    @BeforeEach
    public void setUp() {
        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(120)
                .mpa(new Mpa(1, "G"))
                .build();
    }

    @Test
    public void testAddFilm() {
        Film addedFilm = filmDbStorage.addFilm(testFilm);
        assertThat(addedFilm).isNotNull();
        assertThat(addedFilm.getId()).isGreaterThan(0);
        assertThat(addedFilm.getName()).isEqualTo(testFilm.getName());
    }

    @Test
    public void testUpdateFilm() {
        Film addedFilm = filmDbStorage.addFilm(testFilm);
        addedFilm.setDescription("Updated Description");
        Film updatedFilm = filmDbStorage.updateFilm(addedFilm);

        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    public void testGetFilm() {
        Film addedFilm = filmDbStorage.addFilm(testFilm);
        Film retrievedFilm = (Film) filmDbStorage.getFilms(Collections.singletonList(addedFilm.getId()));

        assertThat(retrievedFilm).isNotNull();
        assertThat(retrievedFilm.getId()).isEqualTo(addedFilm.getId());
    }

    @Test
    public void testDeleteFilm() {
        Film addedFilm = filmDbStorage.addFilm(testFilm);
        filmDbStorage.deleteFilm(addedFilm.getId());

        assertThat(filmDbStorage.getAllFilms()).doesNotContain(addedFilm);
    }
}