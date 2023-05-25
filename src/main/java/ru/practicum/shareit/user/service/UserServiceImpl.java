package ru.practicum.shareit.user.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.exception.UserValidationException;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Qualifier("inMemoryUserRepository")
    @NonNull
    private final UserRepository userRepository;

    @Override
    public Collection<UserDto> getUsers() {
        return userRepository.getUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        return UserMapper.toUserDto(checkUserId(userId));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new UserValidationException("Email не может быть пустым.");
        }

        User user = UserMapper.toUser(userDto);

        try {
            return UserMapper.toUserDto(userRepository.createUser(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(e.getMessage());
        }
    }

    @Override
    public UserDto patchUser(long userId, UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.patchUser(userId, user)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID = " + userId + " не найден.")));
    }

    @Override
    public void removeUser(long userId) {
        checkUserId(userId);
        userRepository.removeUser(userId);
    }

    private User checkUserId(long id) {
        return userRepository.getUserById(id).orElseThrow(()
                -> new UserNotFoundException("Пользователь с ID = " + id + " не найден."));
    }
}
