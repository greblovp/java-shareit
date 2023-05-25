package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    Collection<UserDto> getUsers();

    UserDto getUserById(long userId);

    UserDto createUser(UserDto userDto);

    UserDto patchUser(long userId, UserDto userDto);

    void removeUser(long userId);
}
