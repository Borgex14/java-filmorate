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

    User removeFriend(Long user1, Long user2);

    List<User> getCommonFriends(Long user1Id, Long user2Id);

    List<User> getFriends(Long id);
}
