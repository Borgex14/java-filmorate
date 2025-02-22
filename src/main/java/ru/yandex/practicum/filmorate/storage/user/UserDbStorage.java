package ru.yandex.practicum.filmorate.storage.user;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setTimestamp(4, new Timestamp(user.getBirthday().atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant().getLong(ChronoField.INSTANT_SECONDS)));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user; // Возвращаем пользователя после добавления
    }

    @Override
    public User updateUser(User user) {
        checkUserId(user.getId);
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setTimestamp(4, new Timestamp(user.getBirthday().atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant().getLong(ChronoField.INSTANT_SECONDS)));
            stmt.setLong(5, user.getId());
            return stmt;
        });
        return user; // Возвращаем обновленного пользователя
    }

    @Override
    public User getUser(long id) {
        String sql = "SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT user_id, email, login, name, birthday FROM users";
        List<User> userList = jdbcTemplate.queryForObject(sql, userRowMapper);
        return userList;
    }

    @Override
    public void deleteUser(long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<List<User>> getFriends(Long id) {
        checkUserId(id);
        String sql = "SELECT f.friend_id FROM friends AS f WHERE f.user_id = ?";
        List<User> friends = jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
            long friendId = rs.getLong("friend_id");
            return getUser(friendId);
        });
        return Optional.of(friends);
    }

    private void checkUserId(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE user_id = ?";
        if (jdbcTemplate.queryForObject(sql,Long.class, id) == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
    
}
