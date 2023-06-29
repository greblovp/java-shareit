package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class UserDtoTest {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    public void testValidUserDto() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("John Doe")
                .build();

        assertTrue(validator.validate(userDto).isEmpty());
    }

    @Test
    public void testInvalidEmail() {
        UserDto userDto = UserDto.builder()
                .id(2L)
                .email("invalid-email")
                .name("Jane Smith")
                .build();

        assertFalse(validator.validate(userDto).isEmpty());
    }
}