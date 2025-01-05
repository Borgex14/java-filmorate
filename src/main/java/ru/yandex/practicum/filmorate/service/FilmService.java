package ru.yandex.practicum.filmorate.service;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();
    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getFilm(filmId);
        if (film != null) {
            filmLikes.putIfAbsent(filmId, new HashSet<>());
            if (filmLikes.get(filmId).add(userId)) {
                log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
            } else {
                log.warn("Пользователь с id {} уже поставил лайк фильму с id {}", userId, filmId);
            }
        } else {
            log.error("Не удалось поставить лайк, фильм с id {} не найден в хранилище", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден.");
        }
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getFilm(filmId);
        if (film != null) {
            Set<Long> likes = filmLikes.get(filmId);
            if (likes != null && likes.remove(userId)) {
                log.info("Пользователь с id {} убрал лайк у фильма с id {}", userId, filmId);
            } else {
                log.warn("У пользователя с id {} нет лайка у фильма с id {}", userId, filmId);
            }
        } else {
            throw new NotFoundException("Фильм с id " + filmId + " не найден.");
        }
    }

    public List<Film> getTopFilms(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }

        return filmLikes.entrySet().stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()))
                .limit(count)
                .map(entry -> filmStorage.getFilm(entry.getKey()))
                .collect(Collectors.toList());
    }
}