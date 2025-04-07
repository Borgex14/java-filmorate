package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void testCreateUser() {
        User createdUser = userStorage.createUser(user);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isGreaterThan(0);
        assertThat(createdUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(createdUser.getLogin()).isEqualTo(user.getLogin());
        assertThat(createdUser.getName()).isEqualTo(user.getName());
        assertThat(createdUser.getBirthday()).isEqualTo(user.getBirthday());
    }

    @Test
    void testUpdateUser() {
        User createdUser = userStorage.createUser(user);
        createdUser.setName("Updated User");
        User updatedUser = userStorage.updateUser(createdUser);
        assertThat(updatedUser.getName()).isEqualTo("Updated User");
    }

    @Test
    void testGetUser() {
        User createdUser = userStorage.createUser(user);
        Optional<User> retrievedUser = userStorage.getUser(createdUser.getId());
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getId()).isEqualTo(createdUser.getId());
    }

    @Test
    void testGetAllUsers() {
        userStorage.createUser(user);
        List<User> users = userStorage.getAllUsers();
        assertThat(users).isNotEmpty();
    }

    @Test
    void testDeleteUser() {
        User createdUser = userStorage.createUser(user);
        userStorage.deleteUser(createdUser.getId());
        assertThatThrownBy(() -> userStorage.getUser(createdUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = " + createdUser.getId() + " не найден");
    }

    @Test
    void testAddFriend() {
        User user1 = userStorage.createUser(user);
        User user2 = userStorage.createUser(User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .name("Friend User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
    }

    @Test
    void testRemoveFriend() {
        User user1 = userStorage.createUser(user);
        User user2 = userStorage.createUser(User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .name("Friend User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userStorage.createUser(user);
        User user2 = userStorage.createUser(User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .name("Friend User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        User user3 = userStorage.createUser(User.builder()
                .email("commonFriend@example.com")
                .login("commonFriendLogin")
                .name("Common Friend")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        userStorage.addFriend(user1.getId(), user3.getId());
        userStorage.addFriend(user2.getId(), user3.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(user3.getId());
    }

    @Test
    void testGetFriends() {
        User user1 = userStorage.createUser(user);
        User user2 = userStorage.createUser(User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .name("Friend User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());
        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
    }
}