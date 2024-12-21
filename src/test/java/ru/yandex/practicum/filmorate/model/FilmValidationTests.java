package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FilmValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void shouldValidateCorrectFilm() {
        Film film = new Film();
        film.setName("Valid Film Name");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThat(violations).isEmpty();
    }
}