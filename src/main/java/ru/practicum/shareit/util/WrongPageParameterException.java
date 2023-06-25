package ru.practicum.shareit.util;

public class WrongPageParameterException extends RuntimeException {
    public WrongPageParameterException(String message) {
        super(message);
    }
}
