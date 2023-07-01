package ru.practicum.shareit.request.exception;

public class ItemRequestValidationException extends RuntimeException {
    public ItemRequestValidationException(String message) {
        super(message);
    }
}
