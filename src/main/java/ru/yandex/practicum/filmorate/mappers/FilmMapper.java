package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

public class FilmMapper {

    public static Film toModel(FilmDto filmDto) {
        if (filmDto == null) {
            return null;
        }
        return Film.builder()
                .id(filmDto.getId())
                .name(filmDto.getName())
                .description(filmDto.getDescription())
                .releaseDate(filmDto.getReleaseDate())
                .duration(filmDto.getDuration())
                .mpa(filmDto.getMpa())
                .genres(filmDto.getGenres())
                .build();
    }

    public static FilmDto toFilmDto(Film film) {
        if (film == null) {
            return null;
        }
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .genres(film.getGenres())
                .build();
    }
}