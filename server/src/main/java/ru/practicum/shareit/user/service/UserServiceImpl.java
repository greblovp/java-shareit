package ru.practicum.shareit.user.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @NonNull
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public Collection<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(checkUserId(userId));
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        try {
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public UserDto patchUser(Long userId, UserDto userDto) {
        User userToUpdate = checkUserId(userId);
        if (userDto.getEmail() != null) {
            userToUpdate.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            userToUpdate.setName(userDto.getName());
        }
        return UserMapper.toUserDto(userRepository.save(userToUpdate));
    }

    @Transactional
    @Override
    public void removeUser(Long userId) {
        checkUserId(userId);
        userRepository.deleteById(userId);
    }

    private User checkUserId(Long id) {
        return userRepository.findById(id).orElseThrow(()
                -> new UserNotFoundException("Пользователь с ID = " + id + " не найден."));
    }
}
