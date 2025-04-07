package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {

    Genre getGenreById(Long id);

    Collection<Genre> getAllGenres();

    Collection<Long> findIds();

    Genre getNameById(long id);

    Collection<Genre> getExistGenres(Film film);
}
