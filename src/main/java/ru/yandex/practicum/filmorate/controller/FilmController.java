package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@RequestMapping("/films")
@RestController
@Validated
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @PostMapping
    public ResponseEntity<?> addFilm(@Valid @RequestBody Film film) {
        validateFilm(film);
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return ResponseEntity.ok(film);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film updatedFilm) {
        if (updatedFilm  == null) {
            log.warn("Пустые данные добавлены");
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Название не может быть пустым."));
        }
        try {
            validateFilm(updatedFilm);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
        Long id = updatedFilm.getId();
        if (films.containsKey(id)) {
            films.put(id, updatedFilm);
            log.info("Обновлен фильм с id {}: {}", id, updatedFilm);
            return ResponseEntity.ok(updatedFilm);
        }
        log.warn("Попытка обновления несуществующего фильма с id {}", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "Фильм с id " + id + " не найден."));
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        log.info("Запрос на получение всех фильмов");
        List<Film> filmList = films.values().stream().collect(Collectors.toList());
        log.info("Количество фильмов возвращаемых в ответе: {}", filmList.size());
        return ResponseEntity.ok(filmList);
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Film object is null");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Добавлена дата выхода раньше 28.12.1895");
            throw new ValidationException("Дата не раньше 28.12.1895");
        }
    }
}
