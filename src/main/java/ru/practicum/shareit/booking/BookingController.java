package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BookingValidationException;
import ru.practicum.shareit.booking.exception.BookingWrongStatusException;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid BookingDto bookingDto,
                                    BindingResult bindingResult) {
        log.info("Создать бронирование на вещь ID = {} для пользователя ID = {}", bookingDto.getItemId(), userId);
        generateCustomValidateException(bookingDto, bindingResult);
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        log.info("Подтвердить бронирование ID = {} пользователем ID = {}", bookingId, userId);
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.info("Получить бронирование ID = {} пользователем ID = {}", bookingId, userId);
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingDto> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получить бронирования пользователя ID = {} в состоянии {}", userId, state);
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingWrongStatusException("Unknown state: " + state);
        }
        return bookingService.getAllBookings(userId, bookingState);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получить бронирования владельца ID = {} в состоянии {}", userId, state);
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingWrongStatusException("Unknown state: " + state);
        }
        return bookingService.getAllBookingsByOwner(userId, bookingState);
    }

    private void generateCustomValidateException(BookingDto bookingDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Бронирование - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), bookingDto);
            throw new BookingValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField() + " - " +
                    bindingResult.getFieldError().getDefaultMessage());
        }
    }
}
