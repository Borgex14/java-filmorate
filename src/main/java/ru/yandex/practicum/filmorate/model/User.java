package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import lombok.Data;

@Data
public class User {
    private Long id;
    @NotBlank
    @NotNull(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @")
    private String email;

    @NotNull(message = "Логин не может быть пустым")
    @Size(min = 1, message = "Логин не может содержать пробелы")
    private String login;
    private String name;
    @NotNull
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
