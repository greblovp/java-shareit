package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.exception.UserValidationException;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Вывести всех пользователей");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable long userId) {
        log.info("Вывести пользователя ID = {}", userId);
        return userClient.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserRequestDto requestDto, BindingResult bindingResult) {
        log.info("Создаем пользователя: {}", requestDto);
        generateCustomValidateException(requestDto, bindingResult);
        if (requestDto.getEmail() == null) {
            throw new UserValidationException("Email не может быть пустым.");
        }
        return userClient.createUser(requestDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> patchUser(@RequestBody @Valid UserRequestDto requestDto, BindingResult bindingResult,
                                            @PathVariable long userId) {
        log.info("Обновляем пользователя ID = {}, новые значения {}", userId, requestDto);
        generateCustomValidateException(requestDto, bindingResult);
        return userClient.patchUser(userId, requestDto);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable long userId) {
        log.info("Обновляем пользователя c Id: {}", userId);
        userClient.removeUser(userId);
    }

    private void generateCustomValidateException(UserRequestDto requestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Пользователь - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), requestDto);
            throw new UserValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField());
        }
    }
}
