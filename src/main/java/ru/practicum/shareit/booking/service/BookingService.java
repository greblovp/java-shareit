package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingDto bookingDto);

    BookingDto approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingDto getBooking(Long userId, Long bookingId);

    Collection<BookingDto> getAllBookings(Long userId, BookingState bookingState, Integer from, Integer size);

    Collection<BookingDto> getAllBookingsByOwner(Long userId, BookingState bookingState, Integer from, Integer size);
}
