package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.mappers.UserMapper;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriend(userId, friendId);
        log.info("Пользователь с id {} теперь друг пользователя с id {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь с id {} больше не друг пользователя с id {}", userId, friendId);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public User createUser(UserDto userDto) {
        return userStorage.createUser(UserMapper.toModel(userDto));
    }

    public User updateUser(UserDto userDto) {
        return userStorage.updateUser(UserMapper.toModel(userDto));
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public Optional<User> getUser(Long userId) {
        return userStorage.getUser(userId);
    }

    public void deleteUser(Long userId) {
         userStorage.deleteUser(userId);
    }

    public List<User> getFriends(Long id) {
        return userStorage.getFriends(id);
    }
}