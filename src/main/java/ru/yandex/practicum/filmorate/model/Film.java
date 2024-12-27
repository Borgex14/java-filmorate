package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Data;

@Data
public class Film {
    private Long id;
    @NotBlank
    @NotNull(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;
    @NotNull
    @Past(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;
}