package ru.yandex.practicum.filmorate.controller;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        validateUser(user);
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Создан новый пользователь: {}", user);
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody User updatedUser) {
        if (updatedUser  == null) {
            log.warn("Пустые данные добавлены");
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Данные пользователя не могут быть пустыми."));
        }
        try {
            validateUser(updatedUser);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
        Long id = updatedUser.getId();
        if (users.containsKey(id)) {
            users.put(id, updatedUser);
            log.info("Обновлен пользователь с id {}: {}", id, updatedUser);
            return ResponseEntity.ok(updatedUser);
        }
        log.warn("Попытка обновления несуществующего пользователя с id {}", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "Пользователь с id " + id + " не найден."));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Запрошены все пользователи. Количество пользователей: {}", users.size());
        List<User> userList = users.values().stream().collect(Collectors.toList());
        return ResponseEntity.ok(userList);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть нулевым");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин добавлен");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }
}
