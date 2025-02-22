package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.stream.Collectors;

public class MpaMapper {

    public static Mpa toMpa(MpaDto mpaDto) {
        return Mpa.builder()
                .id(mpaDto.getId())
                .name(mpaDto.getName())
                .build();
    }

    public static MpaDto toMpaDto(Mpa rating) {
        return MpaDto.builder()
                .id(rating.getId())
                .name(rating.getName())
                .build();
    }

    public static List<MpaDto> toMpaDtoList(List<Mpa> ratings) {
        return ratings.stream()
                .map(MpaMapper::toMpaDto)
                .collect(Collectors.toList());
    }

    public static List<Mpa> toMpaList(List<MpaDto> mpaDtos) {
        return mpaDtos.stream()
                .map(MpaMapper::toMpa)
                .collect(Collectors.toList());
    }
}