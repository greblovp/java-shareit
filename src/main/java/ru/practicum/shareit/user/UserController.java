package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserValidationException;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> getUsers() {
        log.info("Вывести всех пользователей");
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable long userId) {
        log.info("Вывести пользователя ID = {}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        log.info("Создаем пользователя: {}", userDto);
        generateCustomValidateException(userDto, bindingResult);
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto patchUser(@RequestBody @Valid UserDto userDto, BindingResult bindingResult, @PathVariable long userId) {
        log.info("Обновляем пользователя ID = {}, новые значения {}", userId, userDto);
        generateCustomValidateException(userDto, bindingResult);
        return userService.patchUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable long userId) {
        log.info("Обновляем пользователя c Id: {}", userId);
        userService.removeUser(userId);
    }

    private void generateCustomValidateException(UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Пользователь - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), userDto);
            throw new UserValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField());
        }
    }
}
