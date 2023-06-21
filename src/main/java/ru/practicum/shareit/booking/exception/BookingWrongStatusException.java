package ru.practicum.shareit.booking.exception;

public class BookingWrongStatusException extends RuntimeException {
    public BookingWrongStatusException(String message) {
        super(message);
    }
}
