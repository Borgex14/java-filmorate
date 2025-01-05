package ru.yandex.practicum.filmorate.controller;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {

    private final UserStorage userStorage;

    public UserController(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User createdUser = userStorage.createUser(user);
        log.info("Создан новый пользователь: {}", createdUser);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody User updatedUser) {
        try {
            User user = userStorage.updateUser(updatedUser);
            log.info("Обновлен пользователь с id {}: {}", user.getId(), user);
            return ResponseEntity.ok(user);
        } catch (ValidationException e) {
            log.warn("Попытка обновления несуществующего пользователя: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Запрошены все пользователи. Количество пользователей: {}", userStorage.getAllUsers().size());
        List<User> userList = userStorage.getAllUsers();
        return ResponseEntity.ok(userList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id) {
        User user = userStorage.getUser(id);
        if (user != null) {
            log.info("Получен пользователь с id {}: {}", id, user);
            return ResponseEntity.ok(user);
        } else {
            log.warn("Пользователь с id {} не найден", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Пользователь с id " + id + " не найден."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        userStorage.deleteUser(id);
        log.info("Удален пользователь с id {}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

