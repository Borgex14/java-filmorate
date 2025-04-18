package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import java.util.Collection;

@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {

        this.genreStorage = genreStorage;
    }

    public Genre getGenreById(Long id) {

        return genreStorage.getGenreById(id);
    }

    public Collection<Genre> getAllGenres() {

        return genreStorage.getAllGenres();
    }

    public Genre getNameById(Long id) {

        return genreStorage.getNameById(id);
    }
}