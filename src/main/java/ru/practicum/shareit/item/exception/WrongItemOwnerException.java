package ru.practicum.shareit.item.exception;

public class WrongItemOwnerException extends RuntimeException {
    public WrongItemOwnerException(String message) {
        super(message);
    }
}
