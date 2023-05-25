package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    Collection<UserDto> getUsers();

    UserDto getUserById(Long userId);

    UserDto createUser(UserDto userDto);

    UserDto patchUser(Long userId, UserDto userDto);

    void removeUser(Long userId);
}
