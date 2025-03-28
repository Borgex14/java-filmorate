package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MpaStorage {

    Mpa getRatingById(int id);

    Collection<Mpa> getAllRatings();

    Integer getCountById(Film film);

    List<Mpa> getListRatingById(List<Integer> mpaIds);
}
