package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.stream.Collectors;

public class FilmMapper {

    public static Film toFilm(FilmDto filmDto) {
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
                .genre(filmDto.getGenres())
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
                .genres(film.getGenre())
                .build();
    }

    public static List<FilmDto> toFilmDtoList(List<Film> films) {
        return films.stream()
                .map(FilmMapper::toFilmDto)
                .collect(Collectors.toList());
    }

    public static List<Film> toFilmList(List<FilmDto> filmDtos) {
        return filmDtos.stream()
                .map(FilmMapper::toFilm)
                .collect(Collectors.toList());
    }
}