package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

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
}