package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mappers.UserMapper;

import jakarta.validation.Valid;
import java.util.List;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto postUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос на добавление пользователя");
        checkUserLogin(userDto);
        return UserMapper.toUserDto(userService.createUser(userDto));
    }

    @PutMapping
    public UserDto putUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос на изменение пользователя.");
        checkUserLogin(userDto);
        userService.updateUser(userDto);
            return userDto;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей.");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        log.info("Получен запрос на получение пользователя по id {}.", id);
        return userService.getUser(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        log.info("Удален пользователь с id {}", id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void postFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Получен запрос от пользователя с id {} на удаление из друзей пользователя с id {}.", friendId, userId);
        userService.removeFriend(userId, friendId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Получен запрос на получение списка всех друзей пользователя с id {}.", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен запрос на получение списка общих друзей пользователей с id {} и id {}.", id, otherId);
        return userService.getCommonFriends(id,otherId);
    }

    private void checkUserLogin(UserDto userDto) {
        if (userDto.getLogin().contains(" ")) {
            log.warn("Логин пользователя {} содержит пробел.", userDto.getLogin());
            throw new ValidationException("Логин не может содержат знак пробела.");
        }
        if (userDto.getName() == null) {
            userDto.setName(userDto.getLogin());
        }
    }
}

