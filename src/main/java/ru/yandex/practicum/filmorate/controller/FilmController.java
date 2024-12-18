package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.yandex.practicum.filmorate.model.Film;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @PostMapping
    public ResponseEntity<Film> addFilm(@RequestBody Film film) {
        film.setId(currentId++);
        films.put(film.getId(), film);
        return ResponseEntity.ok(film);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable Long id, @RequestBody Film updatedFilm) {
        if (films.containsKey(id)) {
            updatedFilm.setId(id);
            films.put(id, updatedFilm);
            return ResponseEntity.ok(updatedFilm);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.ok(films.values().stream().collect(Collectors.toList()));
    }
}
