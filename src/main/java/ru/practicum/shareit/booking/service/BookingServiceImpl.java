package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.BookingValidationException;
import ru.practicum.shareit.booking.exception.BookingWrongStatusException;
import ru.practicum.shareit.booking.exception.WrongBookingUserException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        //Проверяем, что пользователь существует
        User booker = UserMapper.toUser(userService.getUserById(userId));
        //Проверяем, что вещь существует
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(()
                -> new ItemNotFoundException("Вещь с ID = " + bookingDto.getItemId() + " не найдена."));

        //Проверяем, что вещь доступна
        if (!item.getIsAvailable()) {
            throw new ItemNotAvailableException("Вещь с ID = " + bookingDto.getItemId() + " недоступна");
        }
        if (userId.equals(item.getOwnerId())) {
            throw new WrongBookingUserException("Владелец вещи не может забронировать свою вещь");
        }

        Booking bookingToCreate = BookingMapper.toBooking(bookingDto, item, booker);

        checkBookingDates(bookingToCreate);

        bookingToCreate.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(bookingToCreate);

        return BookingMapper.toBookingDto(savedBooking);
    }

    @Transactional
    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        //Проверяем, что пользователь существует
        userService.getUserById(userId);

        Booking bookingToApprove = checkBookingId(bookingId);

        //Проверяем, что запрос делает владелец вещи
        if (!userId.equals(bookingToApprove.getItem().getOwnerId())) {
            throw new WrongItemOwnerException("У вещи с ID = " + bookingToApprove.getItem().getId() + " другой владелец.");
        }

        if (bookingToApprove.getStatus() != BookingStatus.WAITING) {
            throw new BookingWrongStatusException("Для согласования бронирования оно должно быть в статусе WAITING");
        }

        if (approved) {
            bookingToApprove.setStatus(BookingStatus.APPROVED);
        } else {
            bookingToApprove.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.toBookingDto(bookingRepository.save(bookingToApprove));
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Booking bookingToGet = checkBookingId(bookingId);
        Long ownerId = bookingToGet.getItem().getOwnerId();
        Long bookerId = bookingToGet.getBooker().getId();

        boolean isAlowed = userId.equals(ownerId) || userId.equals(bookerId);

        if (!isAlowed) {
            throw new WrongBookingUserException("Пользователь с ID = " + userId + " не является владельцем вещи или " +
                    "автором бронирования");
        }

        return BookingMapper.toBookingDto(bookingToGet);
    }

    @Override
    public Collection<BookingDto> getAllBookings(Long userId, BookingState bookingState) {
        Collection<Booking> bookingList;
        switch (bookingState) {
            case ALL:
                bookingList = bookingRepository.findByBookerIdOrderByStartDateDesc(userId);
                break;
            case CURRENT:
                bookingList = bookingRepository.findByBookerIdAndCurrent(userId, LocalDateTime.now());
                break;
            case PAST:
                bookingList = bookingRepository.findByBookerIdAndEndDateLessThanEqualOrderByStartDateDesc(userId, LocalDateTime.now());
                break;
            case FUTURE:
                bookingList = bookingRepository.findByBookerIdAndStartDateGreaterThanEqualOrderByStartDateDesc(userId, LocalDateTime.now());
                break;
            case WAITING:
                bookingList = bookingRepository.findByBookerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookingList = bookingRepository.findByBookerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                bookingList = new ArrayList<>();
        }
        if (bookingList.isEmpty()) {
            throw new BookingNotFoundException("У пользователя с ID = " + userId + " нет бронирований");
        }
        return BookingMapper.toBookingDto(bookingList);
    }

    @Override
    public Collection<BookingDto> getAllBookingsByOwner(Long userId, BookingState bookingState) {
        Collection<Booking> bookingList;
        switch (bookingState) {
            case ALL:
                bookingList = bookingRepository.findByItem_OwnerIdOrderByStartDateDesc(userId);
                break;
            case CURRENT:
                bookingList = bookingRepository.findByItem_OwnerIdAndCurrent(userId, LocalDateTime.now());
                break;
            case PAST:
                bookingList = bookingRepository.findByItem_OwnerIdAndEndDateLessThanEqualOrderByStartDateDesc(userId, LocalDateTime.now());
                break;
            case FUTURE:
                bookingList = bookingRepository.findByItem_OwnerIdAndStartDateGreaterThanEqualOrderByStartDateDesc(userId, LocalDateTime.now());
                break;
            case WAITING:
                bookingList = bookingRepository.findByItem_OwnerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookingList = bookingRepository.findByItem_OwnerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                bookingList = new ArrayList<>();
        }
        if (bookingList.isEmpty()) {
            throw new BookingNotFoundException("Пользователя с ID = " + userId + " не является владельцем вещей");
        }
        return BookingMapper.toBookingDto(bookingList);
    }

    private Booking checkBookingId(Long id) {
        return bookingRepository.findById(id).orElseThrow(()
                -> new BookingNotFoundException("Бронирование с ID = " + id + " не найдено."));
    }

    private void checkBookingDates(Booking booking) {
        //Проверяема конечная дата бронирования
        LocalDateTime start = booking.getStartDate();
        LocalDateTime end = booking.getEndDate();
        if (start == null) {
            throw new BookingValidationException("Дата начала бронирования должна быть заполнена");
        }
        if (end == null) {
            throw new BookingValidationException("Дата окончания бронирования должна быть заполнена");
        }
        if (end.isBefore(start)) {
            throw new BookingValidationException("Дата оконачания бронирования не может быть раньше даты начала");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BookingValidationException("Дата начала бронирования не может быть раньше текущей даты");
        }
        if (end.isBefore(LocalDateTime.now())) {
            throw new BookingValidationException("Дата оконачания бронирования не может быть раньше текущей даты");
        }
        if (end.isEqual(start)) {
            throw new BookingValidationException("Дата начала и оконачания бронирования не могут совпадать");
        }
    }
}
