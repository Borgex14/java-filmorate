package ru.yandex.practicum.filmorate.storage.user;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import static ru.yandex.practicum.filmorate.exception.GlobalExceptionHandler.log;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        log.info("Creating user with id:{}, email: {}, login: {}, name: {}, birthday: {}",
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            if (user.getBirthday() == null) {
                throw new IllegalArgumentException("Birthday cannot be null");
            }
            stmt.setDate(4, Date.valueOf(user.getBirthday())); // Убедитесь, что user.getBirthday() не null
            return stmt;
        }, keyHolder);

        if (Objects.nonNull(keyHolder.getKey())) {
            long userId = keyHolder.getKey().longValue();
            log.info("Пользователь c login = {} успешно добавлен", user.getLogin());
            return User.builder()
                    .id(userId)
                    .email(user.getEmail())
                    .login(user.getLogin())
                    .name(user.getName())
                    .birthday(user.getBirthday())
                    .build();
        } else {
            throw new NotFoundException("Ошибка добавления пользователя в таблицу");
        }
    }

    @Override
    public User updateUser(User user) {
        checkUserId(user.getId());
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            stmt.setLong(5, user.getId());
            return stmt;
        });
        return user; // Возвращаем обновленного пользователя
    }

    @Override
    public User getUser(long id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT id, email, login, name, birthday FROM users";
        List<User> userList = Collections.singletonList(jdbcTemplate.queryForObject(sql, userRowMapper));
        return userList;
    }

    @Override
    public void deleteUser(long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addFriend(long user1Id, long user2Id) { /// сделать проверку на повторы друзей
        checkUserId(user1Id);
        checkUserId(user2Id);

        String sqlQuery = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setLong(1, user2Id);
            stmt.setLong(2, user1Id);
            return stmt;
        }, keyHolder);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        checkUserId(userId);
        checkUserId(friendId);

        String sqlQuery = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(sqlQuery);
            stmt.setLong(1, userId);
            stmt.setLong(2, friendId);
            return stmt;
        });
    }

    @Override
    public List<User> getCommonFriends(long user1Id, long user2Id) {
        checkUserId(user1Id);
        checkUserId(user2Id);

        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM friendship f1 " +
                "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                "JOIN users u ON f1.friend_id = u.id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), user1Id, user2Id);
    }

    @Override
    public List<User> getFriends(Long id) {
        checkUserId(id);

        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM friendship f JOIN users u ON f.friend_id = u.id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), id);
    }

    private void checkUserId(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?";
        if (jdbcTemplate.queryForObject(sql,Long.class, id) == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}
