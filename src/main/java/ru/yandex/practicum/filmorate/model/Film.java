package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@Data
public class Film {
    Set<Long> likes = new HashSet<>();
    private Long id;
    @NotBlank
    @NotNull(message = "Название не может быть пустым")
    private String name;
    @NotNull
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;
    @NotNull
    @Past(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;
    private Integer rating_id;

    public boolean deleteLike(long userId) {
        if (likes.contains(userId)) {
            likes.remove((Long) userId);
        } else throw new ValidationException("Нет в фильме лайк");
        return false;
    }
}