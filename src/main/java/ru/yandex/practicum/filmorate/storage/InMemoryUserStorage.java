package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public User createUser(User user) {
        validateUser(user);
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User updatedUser) {
        validateUser(updatedUser);
        Long id = updatedUser.getId();
        if (users.containsKey(id)) {
            users.put(id, updatedUser);
            return updatedUser;
        }
        throw new ValidationException("Пользователь с id " + id + " не найден.");
    }

    @Override
    public User getUser(long id) {
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return users.values().stream().collect(Collectors.toList());
    }

    @Override
    public void deleteUser(long id) {
        users.remove(id);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть нулевым.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин добавлен.");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }
}