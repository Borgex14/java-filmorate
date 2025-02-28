package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    User getUser(long id);

    List<User> getAllUsers();

    void deleteUser(long id);

    void addFriend(long user1Id, long user2Id);

    void removeFriend(long user1, long user2);

    List<User> getCommonFriends(long user1Id, long user2Id);

    List<User> getFriends(Long id);
}
