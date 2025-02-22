package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        return User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .login(userDto.getLogin())
                .name(userDto.getName())
                .birthday(userDto.getBirthday())
                .build();
    }

    public static UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    public static List<UserDto> toUserDtoList(List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public static List<User> toUserList(List<UserDto> userDtos) {
        return userDtos.stream()
                .map(UserMapper::toUser)
                .collect(Collectors.toList());
    }
}