package ru.yandex.practicum.filmorate.service;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpaDbStorage;

    public MpaService(MpaStorage mpaDbStorage, NamedParameterJdbcOperations jdbcOperations) {

        this.mpaDbStorage = mpaDbStorage;
    }

    public List<Mpa> getAllRatings() {

        return mpaDbStorage.getAllRatings();
    }

    public Mpa getRatingById(int id) {

        return mpaDbStorage.getRatingById(id);
    }

    public Mpa getNameById(Long id) {

        return mpaDbStorage.getNameById(id);
    }
}