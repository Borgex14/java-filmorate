package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

@Slf4j
@RequestMapping("/films")
@RestController
@RequiredArgsConstructor
@Validated
public class FilmController {
    @Autowired
    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        Film addedFilm = filmService.addFilm(film);
        log.info("Добавлен новый фильм: {}", addedFilm);
        return ResponseEntity.ok(addedFilm);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film updatedFilm) {
        try {
            Film film = filmService.updateFilm(updatedFilm);
            log.info("Обновлен фильм с id {}: {}", film.getId(), film);
            return ResponseEntity.ok(film);
        } catch (ValidationException e) {
            log.warn("Попытка обновления несуществующего фильма: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilms() {
        log.info("Запрос на получение всех фильмов");
        Collection<Film> filmList = filmService.getAllFilms();
        log.info("Количество фильмов возвращаемых в ответе: {}", filmList.size());
        return ResponseEntity.ok(filmList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFilmById(@PathVariable long id) {
        Film film = filmService.getFilmById(id);
            log.info("Получен фильм с id {}: {}", id, film);
            return ResponseEntity.ok(film);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Получен запрос на удаление лайка у фильма с id {} от пользователя с id {}.", filmId, userId);
        filmService.removeLike(filmId, userId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getTopFilms(@RequestParam(value = "count", defaultValue = "10") String count) {
        return filmService.getTopFilms(count);
    }
}

