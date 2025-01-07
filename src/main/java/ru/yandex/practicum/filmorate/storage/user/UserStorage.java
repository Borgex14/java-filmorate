package ru.yandex.practicum.filmorate.storage.user;

import java.util.Optional;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    User getUser(long id);

    List<User> getAllUsers();

    void deleteUser(long id);

    Optional<List<User>> getFriends(Long id);
}
