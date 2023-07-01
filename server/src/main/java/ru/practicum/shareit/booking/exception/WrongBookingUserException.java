package ru.practicum.shareit.booking.exception;

public class WrongBookingUserException extends RuntimeException {
    public WrongBookingUserException(String message) {
        super(message);
    }
}
