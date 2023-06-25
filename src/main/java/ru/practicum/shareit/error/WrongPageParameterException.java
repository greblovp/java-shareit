package ru.practicum.shareit.error;

public class WrongPageParameterException extends RuntimeException {
    public WrongPageParameterException(String message) {
        super(message);
    }
}
