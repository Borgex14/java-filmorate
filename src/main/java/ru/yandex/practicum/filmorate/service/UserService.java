package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.mappers.UserMapper;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        userStorage.getUser(userId);
        log.info("Пользователь с id {} теперь друг пользователя с id {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        userStorage.getUser(userId);
        log.info("Пользователь с id {} больше не друг пользователя с id {}", userId, friendId);
    }

    public List<User> getCommonFriends(long user1Id, long user2Id) {
        return userStorage.getCommonFriends(user1Id, user2Id);
    }

    public User createUser(UserDto userDto) {
        return userStorage.createUser(UserMapper.toModel(userDto));
    }

    public User updateUser(UserDto userDto) {
        return userStorage.updateUser(UserMapper.toModel(userDto));
    }

    public List<UserDto> getAllUsers() {
        return userStorage.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto getUser(Long userId) {
        return UserMapper.toUserDto(userStorage.getUser(userId));
    }

    public void deleteUser(Long userId) {
         userStorage.deleteUser(userId);
    }

    public List<User> getFriends(Long userId) {
        return userStorage.getFriends(userId);
    }
}