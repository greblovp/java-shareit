package ru.practicum.shareit.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.BookingValidationException;
import ru.practicum.shareit.booking.exception.BookingWrongStatusException;
import ru.practicum.shareit.booking.exception.WrongBookingUserException;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.ItemValidationException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.exception.UserValidationException;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(final UserNotFoundException e) {
        return new ErrorResponse("Пользователь не найден", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectUserAttribute(final UserValidationException e) {
        return new ErrorResponse("Ошибка в заполнении полей пользователя", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateUserEmail(final EmailAlreadyExistsException e) {
        return new ErrorResponse("Email уже занят", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleItemNotFound(final ItemNotFoundException e) {
        return new ErrorResponse("Вещь не найдена", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectItemAttribute(final ItemValidationException e) {
        return new ErrorResponse("Ошибка в заполнении полей вещи", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIncorrectItemOwner(final WrongItemOwnerException e) {
        return new ErrorResponse("Некорректный владелец вещи", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleItemNotAvailable(final ItemNotAvailableException e) {
        return new ErrorResponse("Вещь недоступна для бронирования", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleBookingNotFound(final BookingNotFoundException e) {
        return new ErrorResponse("Вещь не найдена", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIncorrectBookingUser(final WrongBookingUserException e) {
        return new ErrorResponse("Некорректный пользователь", e.getMessage());
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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleError(final Throwable e) {
        return new ErrorResponse("Произошла непредвиденная ошибка.", e.getMessage());
    }
}
