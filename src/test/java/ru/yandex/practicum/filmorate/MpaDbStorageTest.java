package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MpaDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MpaDbStorage mpaDbStorage;

    @BeforeEach
    void setUp() {
        mpaDbStorage = new MpaDbStorage(jdbcTemplate, null, new MpaRowMapper());
    }

    @Test
    void testGetAllRatings() {
        List<Mpa> ratings = mpaDbStorage.getAllRatings();
        assertThat(ratings).isNotEmpty();
        assertThat(ratings.size()).isGreaterThan(0);
    }

    @Test
    void testGetCountById_Success() {
        Film film = new Film();
        film.setMpa(new Mpa(1, "G"));

        Integer count = mpaDbStorage.getCountById(film);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testGetCountById_NotFound() {
        Film film = new Film();
        film.setMpa(new Mpa(999, "Unknown"));

        assertThatThrownBy(() -> mpaDbStorage.getCountById(film))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("MPA id не существует");
    }
}