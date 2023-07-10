package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.exception.BookingValidationException;
import ru.practicum.shareit.booking.exception.BookingWrongStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestBody @Valid BookItemRequestDto requestDto,
                                                BindingResult bindingResult) {
        generateCustomValidateException(requestDto, bindingResult);
        checkBookingDates(requestDto);
        log.info("Создать бронирование на вещь ID = {} для пользователя ID = {}", requestDto.getItemId(), userId);
        return bookingClient.createBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId,
                                                 @RequestParam(name = "approved") Boolean approved) {
        log.info("Подтвердить бронирование ID = {} пользователем ID = {}", bookingId, userId);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.info("Получить бронирование ID = {} пользователем ID = {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получить бронирования пользователя ID = {} в состоянии {}", userId, stateParam);
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new BookingWrongStatusException("Unknown state: " + stateParam));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                     @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получить бронирования владельца ID = {} в состоянии {}", userId, stateParam);
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new BookingWrongStatusException("Unknown state: " + stateParam));
        return bookingClient.getAllBookingsByOwner(userId, state, from, size);
    }

    private void generateCustomValidateException(BookItemRequestDto requestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Бронирование - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), requestDto);
            throw new BookingValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField() + " - " +
                    bindingResult.getFieldError().getDefaultMessage());
        }
    }

    private void checkBookingDates(BookItemRequestDto requestDto) {
        //Проверяема конечная дата бронирования
        LocalDateTime start = requestDto.getStart();
        LocalDateTime end = requestDto.getEnd();
        if (end.isBefore(start)) {
            throw new BookingValidationException("Дата оконачания бронирования не может быть раньше даты начала");
        }
        if (end.isEqual(start)) {
            throw new BookingValidationException("Дата начала и оконачания бронирования не могут совпадать");
        }
    }


}
