package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    Optional<User> getUser(Long id);

    List<User> getAllUsers();

    void deleteUser(Long id);

    void addFriend(Long userId, Long friendId);

    User removeFriend(Long userId, Long friendId);

    List<User> getCommonFriends(Long id, Long otherId);

    List<User> getFriends(Long id);
}
