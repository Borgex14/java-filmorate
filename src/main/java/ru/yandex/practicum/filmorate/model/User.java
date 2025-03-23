package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    public Long id;
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

   // Set<User> friends = new HashSet<>();
}
