package ru.practicum.shareit.item.error;

public class CommentValidationException extends RuntimeException {
    public CommentValidationException(String message) {
        super(message);
    }
}
