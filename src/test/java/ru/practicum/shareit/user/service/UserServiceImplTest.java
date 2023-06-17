package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.exception.UserValidationException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private final UserService userService = new UserServiceImpl(userRepository);

    private UserDto userDto = UserDto.builder()
            .email("test1@test.test")
            .name("name1")
            .build();

    private UserDto userDtoNullEmail = UserDto.builder()
            .name("name1")
            .build();

    private User user1 = new User();
    private User user2 = new User();

    UserServiceImplTest() {
        user1.setName("name1");
        user1.setEmail("test1@test.test");

        user2.setName("name2");
        user2.setEmail("test2@test.test");
    }

    @Test
    public void testFindAll() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        Collection<UserDto> result = userService.getUsers();

        assertEquals(2, result.size());
        assertEquals(List.of(UserMapper.toUserDto(user1), UserMapper.toUserDto(user2)), result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testFindById() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        UserDto result = userService.getUserById(userId);

        assertEquals(userDto, result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testFindByIdNotFound() {
        Long userId = 3L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");
    }


    @Test
    void testCreateUser_whenEmailIsNull() {
        assertThatThrownBy(() -> userService.createUser(userDtoNullEmail))
                .isInstanceOf(UserValidationException.class)
                .hasMessage("Email не может быть пустым.");
    }

    @Test
    void testCreateUser_whenEmailAlreadyExists() {
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    public void testUpdateUser() {
        Long userId = 3L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(userRepository.save(user1)).thenReturn(user1);

        UserDto result = userService.patchUser(userId, userDto);

        assertEquals(userDto, result);
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void testUpdateUser_whenUserNotFound() {
        Long userId = 3L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.patchUser(userId, userDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");
    }

    @Test
    void removeUser_removesUser_whenUserExists() {
        Long userId = 3L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        userService.removeUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void removeUser_whenUserNotFound() {
        Long userId = 3L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.removeUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");
    }

}