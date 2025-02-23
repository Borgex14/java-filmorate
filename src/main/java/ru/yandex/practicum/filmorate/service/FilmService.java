package ru.yandex.practicum.filmorate.service;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public void addLike(long filmId, long userId) {
        userStorage.getUser(userId);
        Film film = filmStorage.getFilm(filmId);
        if (film.getLikes().add(userId)) {
            log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
        } else {
            log.warn("Пользователь с id {} уже поставил лайк фильму с id {}", userId, filmId);
        }
    }

    public void removeLike(long filmId, long userId) {
        userStorage.getUser(userId);
        if (filmStorage.getFilm(filmId).deleteLike(userId)) {
            log.info("Пользователь с id {} убрал лайк у фильма с id {}", userId, filmId);
        } else {
            log.warn("У пользователя с id {} нет лайка у фильма с id {}", userId, filmId);
        }
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilm(id);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public List<Film> getTopFilms(Integer count) {
        return filmStorage.getAllFilms().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getLikes().size(), entry1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}