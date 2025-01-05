package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        if (friend == null) {
            throw new NotFoundException("Друга с id " + friendId + " не найден.");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь с id {} теперь друг пользователя с id {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        if (friend == null) {
            throw new NotFoundException("Друга с id " + friendId + " не найден.");
        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь с id {} больше не друг пользователя с id {}", userId, friendId);
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        User user = userStorage.getUser(userId);
        User otherUser = userStorage.getUser(otherUserId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        if (otherUser == null) {
            throw new NotFoundException("Пользователь с id " + otherUserId + " не найден.");
        }
        Set<Long> commonFriendsIds = new HashSet<>(user.getFriends());
        commonFriendsIds.retainAll(otherUser.getFriends());
        return commonFriendsIds.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toList());
    }
}