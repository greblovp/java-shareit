package ru.practicum.shareit.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.BookingValidationException;
import ru.practicum.shareit.booking.exception.BookingWrongStatusException;
import ru.practicum.shareit.item.error.CommentValidationException;
import ru.practicum.shareit.item.error.ItemValidationException;
import ru.practicum.shareit.request.exception.ItemRequestValidationException;
import ru.practicum.shareit.user.exception.UserValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectUserAttribute(final UserValidationException e) {
        return new ErrorResponse("Ошибка в заполнении полей пользователя", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectCommentAttribute(final CommentValidationException e) {
        return new ErrorResponse("Ошибка в заполнении полей комментария", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectItemAttribute(final ItemValidationException e) {
        return new ErrorResponse("Ошибка в заполнении полей вещи", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectBookingAttribute(final BookingValidationException e) {
        return new ErrorResponse("Ошибка в заполнении полей бронирования", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectBookingStatus(final BookingWrongStatusException e) {
        return new ErrorResponse(e.getMessage(), e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectItemRequestAttribute(final ItemRequestValidationException e) {
        return new ErrorResponse("Ошибка в заполнении полей запроса", e.getMessage());
    }
}
