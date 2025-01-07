package ru.yandex.practicum.filmorate.storage.user;

import java.util.ArrayList;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
    public User getUser(long userId) {
        if (!users.containsKey(userId)) {
        log.error("Пользователь с id {} не найден", userId);
        throw new NotFoundException("Пользователь с id " + userId + " не найден.");
    }
        return users.get(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return users.values().stream().collect(Collectors.toList());
    }

    @Override
    public void deleteUser(long id) {
        users.remove(id);
    }

    @Override
    public Optional<List<User>> getFriends(Long id) {
        List<User> result = new ArrayList<>();
        if(users.isEmpty()) {
            log.error("Ошибка при получении списка юзеров");
            return Optional.empty();
        }
        if (!users.containsKey(id)) {
            throw new NotFoundException("User c id = " + id + " не найден");
        }
        if (users.containsKey(id) && !users.get(id).getFriends().isEmpty()) {
            for (Long userFriendId : users.get(id).getFriends()) {
                result.add(users.get(userFriendId));
            }
            return Optional.of(result);
        }
        log.error("Ошибка при получении списка юзеров");
        return Optional.empty();
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