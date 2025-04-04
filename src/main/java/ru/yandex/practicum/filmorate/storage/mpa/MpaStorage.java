package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {

    Mpa getRatingById(int id);

    List<Mpa> getAllRatings();

    Integer getCountById(Film film);

    List<Mpa> getListRatingById(List<Integer> mpaIds);
}
