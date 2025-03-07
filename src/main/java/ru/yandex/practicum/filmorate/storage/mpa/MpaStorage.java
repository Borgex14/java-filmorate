package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {

    Mpa getRatingById(int id);

    List<Mpa> getAllRatings();

    Mpa getNameById(Long id);

    Integer getCountById(Film film);
}
