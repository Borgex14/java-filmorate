package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.stream.Collectors;

public class GenreMapper {

    public static Genre toGenre(GenreDto genreDto) {
        return Genre.builder()
                .id(genreDto.getId())
                .name(genreDto.getName())
                .build();
    }

    public static GenreDto toGenreDto(Genre genre) {
        return GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public static List<GenreDto> toGenreDtoList(List<Genre> genres) {
        return genres.stream()
                .map(GenreMapper::toGenreDto)
                .collect(Collectors.toList());
    }

    public static List<Genre> toGenreList(List<GenreDto> genreDtos) {
        return genreDtos.stream()
                .map(GenreMapper::toGenre)
                .collect(Collectors.toList());
    }
}