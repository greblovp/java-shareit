package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.WrongBookingUserException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.PageGetter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Service
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
            throw new IllegalArgumentException("Для согласования бронирования оно должно быть в статусе WAITING");
        }

        if (approved) {
            bookingToApprove.setStatus(BookingStatus.APPROVED);
        } else {
            bookingToApprove.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.toBookingDto(bookingRepository.save(bookingToApprove));
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingDto> getAllBookings(Long userId, BookingState bookingState, Integer from, Integer size) {
        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("endDate").descending());

        Collection<Booking> bookingList;
        switch (bookingState) {
            case ALL:
                bookingList = bookingRepository.findByBookerId(userId, page).getContent();
                break;
            case CURRENT:
                bookingList = bookingRepository.findByBookerIdAndCurrent(userId, LocalDateTime.now(), page).getContent();
                break;
            case PAST:
                bookingList = bookingRepository.findByBookerIdAndEndDateLessThanEqual(userId, LocalDateTime.now(), page).getContent();
                break;
            case FUTURE:
                bookingList = bookingRepository.findByBookerIdAndStartDateGreaterThanEqual(userId, LocalDateTime.now(), page).getContent();
                break;
            case WAITING:
                bookingList = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, page).getContent();
                break;
            case REJECTED:
                bookingList = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, page).getContent();
                break;
            default:
                bookingList = new ArrayList<>();
        }
        if (bookingList.isEmpty()) {
            throw new BookingNotFoundException("У пользователя с ID = " + userId + " нет бронирований");
        }
        return BookingMapper.toBookingDto(bookingList);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingDto> getAllBookingsByOwner(Long userId, BookingState bookingState, Integer from, Integer size) {
        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("endDate").descending());

        Collection<Booking> bookingList;
        switch (bookingState) {
            case ALL:
                bookingList = bookingRepository.findByItem_OwnerId(userId, page).getContent();
                break;
            case CURRENT:
                bookingList = bookingRepository.findByItem_OwnerIdAndCurrent(userId, LocalDateTime.now(), page).getContent();
                break;
            case PAST:
                bookingList = bookingRepository.findByItem_OwnerIdAndEndDateLessThanEqual(userId, LocalDateTime.now(), page).getContent();
                break;
            case FUTURE:
                bookingList = bookingRepository.findByItem_OwnerIdAndStartDateGreaterThanEqual(userId, LocalDateTime.now(), page).getContent();
                break;
            case WAITING:
                bookingList = bookingRepository.findByItem_OwnerIdAndStatus(userId, BookingStatus.WAITING, page).getContent();
                break;
            case REJECTED:
                bookingList = bookingRepository.findByItem_OwnerIdAndStatus(userId, BookingStatus.REJECTED, page).getContent();
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
}
