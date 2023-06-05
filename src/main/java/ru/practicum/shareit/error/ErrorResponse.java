package ru.practicum.shareit.error;

import lombok.Data;

@Data
public class ErrorResponse {
    // название ошибки
    private final String error;
    // подробное описание
    private final String description;
}
