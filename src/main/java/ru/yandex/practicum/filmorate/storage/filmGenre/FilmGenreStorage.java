package ru.yandex.practicum.filmorate.storage.filmGenre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmGenreStorage {

    Map<Long, Set<Genre>> getGenresByFilmIds(List<Long> filmIds);

    Collection<Genre> findAll();

    Collection<Genre> findByFilmId(Long id);

    void addGenresInFilmGenres(Film film, Long newId);

    List<Genre> getListGenreFromDbGenres(Long filmId);
}
