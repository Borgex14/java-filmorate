package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @Override
    public Film addFilm(Film film) {
        validateFilm(film);
        film.setId(currentId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        validateFilm(updatedFilm);
        Long id = updatedFilm.getId();
        if (films.containsKey(id)) {
            films.put(id, updatedFilm);
            return updatedFilm;
        }
        throw new ValidationException("Фильм с id " + id + " не найден.");
    }

    @Override
    public Film getFilm(long id) {
        return films.get(id);
    }

    @Override
    public List<Film> getAllFilms() {
        return films.values().stream().collect(Collectors.toList());
    }

    @Override
    public void deleteFilm(long id) {
        films.remove(id);
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Film object is null");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата не может быть раньше 28.12.1895");
        }
    }
}