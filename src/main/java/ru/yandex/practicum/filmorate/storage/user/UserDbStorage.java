package ru.yandex.practicum.filmorate.storage.user;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import static ru.yandex.practicum.filmorate.exception.GlobalExceptionHandler.log;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
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
        log.info("Creating user with id:{}, email: {}, login: {}, name: {}, birthday: {}",
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        if (Objects.nonNull(keyHolder.getKey())) {
            long userId = keyHolder.getKey().longValue();
            log.info("Пользователь c login = {} успешно создан", user.getLogin());
            return User.builder()
                    .id(userId)
                    .email(user.getEmail())
                    .login(user.getLogin())
                    .name(user.getName())
                    .birthday(user.getBirthday())
                    .build();
        } else {
            throw new NotFoundException("Ошибка создания пользователя в таблицу");
        }
    }

    @Override
    public User updateUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final long userId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getBirthday().toString());
            stmt.setLong(5, user.getId());
            return stmt;
        }, keyHolder);

        if (Objects.nonNull(keyHolder.getKey())) {
            userId = keyHolder.getKey().longValue();
        } else {
            throw new NotFoundException("Ошибка обновления пользователя");
        }

        User resultUser = User.builder()
                .id(userId)
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();

        if (rows > 0) {
            log.info("Пользователь с id = {} успешно обновлён", userId);
            return resultUser;

        } else {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    @Override
    public Optional<User> getUser(Long id) {
        Optional<User> resultUser;

        String sqlQuery = "SELECT id, email, login, name, birthday " +
                "from users where id = ?";

        try {
            resultUser = Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery,
                    userRowMapper::mapRow, id));
        } catch (EmptyResultDataAccessException e) {
            resultUser = Optional.empty();
        }

        if (resultUser.isPresent()) {
            return resultUser;

        } else {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT id, email, login, name, birthday FROM users";
        return jdbcTemplate.query(sql, userRowMapper::mapRow);
    }

    @Override
    public void deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addFriend(Long user1Id, Long user2Id) { /// сделать проверку на повторы друзей
        checkUserId(user1Id);
        checkUserId(user2Id);
        String checkFriendshipQuery = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Long count = jdbcTemplate.queryForObject(checkFriendshipQuery, Long.class, user1Id, user2Id);

        if (count != null && count > 0) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }

        log.info("Получен запрос на получение пользователя по id {}.", user1Id);
        Optional<User> mainUser = getUser(user1Id);
        log.info("Получен запрос на получение пользователя по id {}.", user2Id);
        Optional<User> friendUser = getUser(user2Id);

        if (mainUser.isPresent() && friendUser.isPresent()) {
        String sqlQuery = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"friendship_id"});
                stmt.setLong(1, user1Id);
                stmt.setLong(2, user2Id);
                return stmt;
            }, keyHolder);

            log.info("Пользователь с id = {} добавил в друзья пользователя с id = {}", user1Id, user2Id);

        } else if (mainUser.isEmpty()) {
            log.error("Пользователь с id = {} не существует ", user1Id);
            throw new NotFoundException("Пользователь с id = " + user1Id + " не найден");
        } else {
            log.error("Нет пользователя с id = {} ", user2Id);
            throw new NotFoundException("Пользователь с id = " + user2Id + " не найден");
        }
    }

    @Override
    public User removeFriend(Long mainUserId, Long friendUserId) {
        checkUserId(mainUserId);
        checkUserId(friendUserId);
        if (Objects.equals(mainUserId, friendUserId)) {
            log.error("Нельзя добавить в друзья самого себя");
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }
        Optional<User> mainUser = getUser(mainUserId);
        Optional<User> friendUser = getUser(friendUserId);

        if (mainUser.isPresent() && friendUser.isPresent()) {
            String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

            int deletedRows = jdbcTemplate.update(sql, mainUserId, friendUserId);
            log.info("Удалено {} строк", deletedRows);

            log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}", mainUserId, friendUserId);
            return mainUser.get();
        } else if (mainUser.isEmpty()) {
        log.error("Пользователя с id = {} нет", mainUserId);
        throw new NotFoundException("Пользователь с id = " + mainUserId + " не найден");

        } else {
        log.error("Нет такого пользователя с id = {} ", friendUserId);
        throw new NotFoundException("Пользователь с id = " + friendUserId + " не найден");
        }
    }

    @Override
    public List<User> getCommonFriends(Long user1Id, Long user2Id) {
        checkUserId(user1Id);
        checkUserId(user2Id);

        String sql = "SELECT id, email, login, name, birthday FROM users " +
                "JOIN friendships AS fri ON users.id = fri.friend_id " +
                "JOIN friendships AS fri2 ON users.id = fri2.friend_id " +
                "WHERE fri.user_id = ? AND fri2.user_id = ? ";
        if (getUser(user1Id).isPresent() && getUser(user2Id).isPresent()) {
        return jdbcTemplate.query(sql, new UserRowMapper(), user1Id, user2Id);
        } else if (getUser(user1Id).isEmpty()) {
            log.error("К сожалению пользователь с id = {} не найден", user1Id);
            throw new NotFoundException("Пользователь с id = " + user1Id + " не найден");

        } else {
            log.error("Этот пользователь с id = {} не найден", user2Id);
            throw new NotFoundException("Пользователь с id = " + user2Id + " не найден");
        }
    }

    @Override
    public List<User> getFriends(Long id) {
        checkUserId(id);
        String sqlQueryUser2 = "SELECT friend_id " +
                "FROM friendships WHERE user_id = ?";
        List<Long> friendsId = jdbcTemplate.queryForList(sqlQueryUser2, Long.class, id);

        List<User> result = new ArrayList<>();

        for (Long friendId : friendsId) {
            // Получаем пользователя по его идентификатору
            Optional<User> friend = getUser(friendId);
            friend.ifPresent(result::add); // Добавляем пользователя в результат, если он существует
        }

        return result;
    }

    private void checkUserId(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)";
        if (jdbcTemplate.queryForObject(sql,Long.class, id) == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}
