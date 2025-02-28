package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserDbStorageTest {

    @Autowired
    private final UserDbStorage userDbStorage;

    User testUser = User.builder().id(1).login("mlll").email("pl@mail.ru").name("Tania")
            .birthday(LocalDate.of(1980,12,6)).build();
    User postTestUser = User.builder().id(2).login("urt").email("jim@mail.com").name("Ana")
            .birthday(LocalDate.of(1967, 4,7)).build();
    User putTestUser = User.builder().id(1).login("urt").email("jim@mail.com").name("Nastia")
            .birthday(LocalDate.of(1977, 3,7)).build();


    @Test
    public void testGetUserById() {

        Optional<User> userOptional = Optional.ofNullable(userDbStorage.getUser(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                )
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(testUser));
    }

    @Test
    public void  testPostUser() {

        Optional<User> userOptional = Optional.ofNullable(userDbStorage.createUser(User.builder().login("Dina")
                .email("jdn@mail.com").name("Anabela").birthday(LocalDate.of(1997, 4,7)).build()));

        assertThat(userOptional)
                .isPresent()
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(postTestUser));
    }

    @Test
    public void testPutUser() {
        Optional<User> userOptional = Optional.ofNullable(userDbStorage.updateUser(User.builder().id(1).login("urt")
                .email("jim@mail.com").name("Marina").birthday(LocalDate.of(1977, 9,10)).build()));

        assertThat(userOptional)
                .isPresent()
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(putTestUser));
    }
}