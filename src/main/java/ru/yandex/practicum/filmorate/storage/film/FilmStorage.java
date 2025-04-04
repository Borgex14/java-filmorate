package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    //Film getFilm(long id);

    List<Film> getFilms(List<Long> filmIds);

    List<Film> getAllFilms();

    void deleteFilm(long id);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Film> getTopFilms(String count);
}