package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Like {
    private Integer likeId;
    private Integer userId;
    private Integer filmId;
}
