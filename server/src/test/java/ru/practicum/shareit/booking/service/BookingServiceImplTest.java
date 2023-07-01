package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.BookingWrongStatusException;
import ru.practicum.shareit.booking.exception.WrongBookingUserException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@TestPropertySource(properties = {"db.name=test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DataJpaTest
class BookingServiceImplTest {

    private final TestEntityManager em;
    private final BookingRepository bookingRepository;
    @MockBean
    private UserService userService;
    private final ItemRepository itemRepository;

    private BookingServiceImpl bookingService;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userService, itemRepository);
    }

    @Test
    void testCreateBooking() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());

        sourceUserBookerDto.setId(userBookerId);
        when(userService.getUserById(userBookerId)).thenReturn(sourceUserBookerDto);

        // when
        BookingDto targetBookingDto = bookingService.createBooking(userBookerId, sourceBookingDto);

        // then
        assertThat(targetBookingDto.getId(), notNullValue());
        assertThat(targetBookingDto.getStart(), equalTo(sourceBookingDto.getStart()));
        assertThat(targetBookingDto.getEnd(), equalTo(sourceBookingDto.getEnd()));
        assertThat(targetBookingDto.getStatus(), equalTo(BookingStatus.WAITING));

        Booking persistedBooking = em.find(Booking.class, targetBookingDto.getId());
        assertThat(persistedBooking, notNullValue());
        assertThat(persistedBooking.getStartDate(), equalTo(starDate));
        assertThat(persistedBooking.getEndDate(), equalTo(endDate));
        assertThat(persistedBooking.getItem(), equalTo(itemEntity));
        assertThat(persistedBooking.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(persistedBooking.getBooker(), equalTo(userBookerEntity));

        verify(userService).getUserById(userBookerId);
    }

    @Test
    void testCreateBooking_whenItemNotFound() {
        // given
        Long itemId = 1L;
        Long userId = 2L;
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());

        when(userService.getUserById(userId)).thenReturn(sourceUserBookerDto);

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(userId, sourceBookingDto))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("Вещь с ID = " + itemId + " не найдена.");

        verify(userService).getUserById(userId);
    }

    @Test
    void testCreateBooking_whenItemNotAvailable() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", false);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());

        sourceUserBookerDto.setId(userBookerId);
        when(userService.getUserById(userBookerId)).thenReturn(sourceUserBookerDto);

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(userBookerId, sourceBookingDto))
                .isInstanceOf(ItemNotAvailableException.class)
                .hasMessage("Вещь с ID = " + itemId + " недоступна");

        verify(userService).getUserById(userBookerId);
    }

    @Test
    void testCreateBooking_whenBookingAsOwner() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());

        sourceUserDto.setId(userId);
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(userId, sourceBookingDto))
                .isInstanceOf(WrongBookingUserException.class)
                .hasMessage("Владелец вещи не может забронировать свою вещь");

        verify(userService).getUserById(userId);
    }

    @Test
    void testApproveBooking_bookingApproved() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());
        Booking bookingEntity = BookingMapper.toBooking(sourceBookingDto, itemEntity, userBookerEntity);
        bookingEntity.setStatus(BookingStatus.WAITING);
        em.persist(bookingEntity);
        em.flush();
        Long bookingId = bookingEntity.getId();

        sourceUserOwnerDto.setId(userOwnerId);
        when(userService.getUserById(userOwnerId)).thenReturn(sourceUserOwnerDto);

        // when
        BookingDto targetBookingDto = bookingService.approveBooking(userOwnerId, bookingId, true);

        // then
        assertThat(targetBookingDto.getId(), notNullValue());
        assertThat(targetBookingDto.getStatus(), equalTo(BookingStatus.APPROVED));

        Booking persistedBooking = em.find(Booking.class, targetBookingDto.getId());
        assertThat(persistedBooking, notNullValue());
        assertThat(persistedBooking.getStatus(), equalTo(BookingStatus.APPROVED));

        verify(userService).getUserById(userOwnerId);
    }

    @Test
    void testApproveBooking_whenWrongItemOwner() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());
        Booking bookingEntity = BookingMapper.toBooking(sourceBookingDto, itemEntity, userBookerEntity);
        bookingEntity.setStatus(BookingStatus.WAITING);
        em.persist(bookingEntity);
        em.flush();
        Long bookingId = bookingEntity.getId();

        sourceUserBookerDto.setId(userBookerId);
        when(userService.getUserById(userBookerId)).thenReturn(sourceUserBookerDto);

        // when & then
        assertThatThrownBy(() -> bookingService.approveBooking(userBookerId, bookingId, true))
                .isInstanceOf(WrongItemOwnerException.class)
                .hasMessage("У вещи с ID = " + itemId + " другой владелец.");

        verify(userService).getUserById(userBookerId);
    }

    @Test
    void testApproveBooking_whenWrongStatus() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());
        Booking bookingEntity = BookingMapper.toBooking(sourceBookingDto, itemEntity, userBookerEntity);
        bookingEntity.setStatus(BookingStatus.REJECTED);
        em.persist(bookingEntity);
        em.flush();
        Long bookingId = bookingEntity.getId();

        sourceUserOwnerDto.setId(userOwnerId);
        when(userService.getUserById(userOwnerId)).thenReturn(sourceUserOwnerDto);

        // when & then
        assertThatThrownBy(() -> bookingService.approveBooking(userOwnerId, bookingId, true))
                .isInstanceOf(BookingWrongStatusException.class)
                .hasMessage("Для согласования бронирования оно должно быть в статусе WAITING");

        verify(userService).getUserById(userOwnerId);
    }

    @Test
    void testGetBooking() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());
        Booking bookingEntity = BookingMapper.toBooking(sourceBookingDto, itemEntity, userBookerEntity);
        bookingEntity.setStatus(BookingStatus.APPROVED);
        em.persist(bookingEntity);
        em.flush();
        Long bookingId = bookingEntity.getId();

        // when
        BookingDto targetBookingDto = bookingService.getBooking(userOwnerId, bookingId);

        // then
        assertThat(targetBookingDto.getId(), equalTo(bookingEntity.getId()));
        assertThat(targetBookingDto.getStatus(), equalTo(bookingEntity.getStatus()));
        assertThat(targetBookingDto.getBooker().getId(), equalTo(userBookerEntity.getId()));
        assertThat(targetBookingDto.getItem().getId(), equalTo(itemEntity.getId()));
    }


    @Test
    public void testGetBooking_whenNotAllowedUser() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());
        Booking bookingEntity = BookingMapper.toBooking(sourceBookingDto, itemEntity, userBookerEntity);
        bookingEntity.setStatus(BookingStatus.APPROVED);
        em.persist(bookingEntity);
        em.flush();
        Long bookingId = bookingEntity.getId();

        // when & then
        assertThatThrownBy(() -> bookingService.getBooking(userOwnerId + userBookerId, bookingId))
                .isInstanceOf(WrongBookingUserException.class)
                .hasMessage("Пользователь с ID = " + (userOwnerId + userBookerId) + " не является владельцем вещи или " +
                        "автором бронирования");
    }

    @Test
    public void testGetBooking_whenBookingNotFound() {
        // given
        Long userBookerId = 1L;
        Long bookingId = 2L;

        // when & then
        assertThatThrownBy(() -> bookingService.getBooking(userBookerId, bookingId))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Бронирование с ID = " + bookingId + " не найдено.");
    }


    @Test
    public void testGetAllBookingsWithAllState() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        List<BookingDto> sourceBookingDtos = List.of(
                makeBookingDto(itemId, starDate.toString(), endDate.toString()),
                makeBookingDto(itemId, starDate.plusDays(1).toString(), endDate.plusDays(1).toString()),
                makeBookingDto(itemId, starDate.plusDays(2).toString(), endDate.plusDays(2).toString())
        );

        for (BookingDto bookingDto : sourceBookingDtos) {
            Booking bookingEntity = BookingMapper.toBooking(bookingDto, itemEntity, userBookerEntity);
            bookingEntity.setStatus(BookingStatus.REJECTED);
            em.persist(bookingEntity);
        }
        em.flush();

        BookingState bookingState = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        // when
        Collection<BookingDto> targetBookingDtos = bookingService.getAllBookings(userBookerId, bookingState, from, size);

        // then
        assertThat(targetBookingDtos, hasSize(sourceBookingDtos.size()));
        for (BookingDto sourceBookingDto : sourceBookingDtos) {
            assertThat(targetBookingDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBookingDto.getStart())),
                    hasProperty("end", equalTo(sourceBookingDto.getEnd())),
                    hasProperty("status", equalTo(BookingStatus.REJECTED)))
            ));
        }
    }

    @Test
    public void testGetAllBookings_whenNoBookings() {
        // given
        Long userBookerId = 1L;
        BookingState bookingState = BookingState.FUTURE;
        Integer from = 0;
        Integer size = 10;

        // when & then
        assertThatThrownBy(() -> bookingService.getAllBookings(userBookerId, bookingState, from, size))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("У пользователя с ID = " + userBookerId + " нет бронирований");
    }

    @Test
    public void testGetAllBookings_whenNoBookings_PAST() {
        // given
        Long userBookerId = 1L;
        BookingState bookingState = BookingState.PAST;
        Integer from = 0;
        Integer size = 10;

        // when & then
        assertThatThrownBy(() -> bookingService.getAllBookings(userBookerId, bookingState, from, size))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("У пользователя с ID = " + userBookerId + " нет бронирований");
    }

    @Test
    public void testGetAllBookings_whenNoBookings_WAITING() {
        // given
        Long userBookerId = 1L;
        BookingState bookingState = BookingState.WAITING;
        Integer from = 0;
        Integer size = 10;

        // when & then
        assertThatThrownBy(() -> bookingService.getAllBookings(userBookerId, bookingState, from, size))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("У пользователя с ID = " + userBookerId + " нет бронирований");
    }

    @Test
    public void testGetAllBookings_whenNoBookings_CURRENT() {
        // given
        Long userBookerId = 1L;
        BookingState bookingState = BookingState.CURRENT;
        Integer from = 0;
        Integer size = 10;

        // when & then
        assertThatThrownBy(() -> bookingService.getAllBookings(userBookerId, bookingState, from, size))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("У пользователя с ID = " + userBookerId + " нет бронирований");
    }

    @Test
    public void testGetAllBookings_whenNoBookings_REJECTED() {
        // given
        Long userBookerId = 1L;
        BookingState bookingState = BookingState.REJECTED;
        Integer from = 0;
        Integer size = 10;

        // when & then
        assertThatThrownBy(() -> bookingService.getAllBookings(userBookerId, bookingState, from, size))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("У пользователя с ID = " + userBookerId + " нет бронирований");
    }

    @Test
    void testGetAllBookingsByOwner_whenBookingState_WAITING() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        List<BookingDto> sourceBookingDtos = List.of(
                makeBookingDto(itemId, starDate.toString(), endDate.toString()),
                makeBookingDto(itemId, starDate.plusDays(1).toString(), endDate.plusDays(1).toString()),
                makeBookingDto(itemId, starDate.plusDays(2).toString(), endDate.plusDays(2).toString())
        );

        for (BookingDto bookingDto : sourceBookingDtos) {
            Booking bookingEntity = BookingMapper.toBooking(bookingDto, itemEntity, userBookerEntity);
            bookingEntity.setStatus(BookingStatus.WAITING);
            em.persist(bookingEntity);
        }
        em.flush();

        BookingState bookingState = BookingState.WAITING;
        Integer from = 0;
        Integer size = 10;

        // when
        Collection<BookingDto> targetBookingDtos = bookingService.getAllBookingsByOwner(userOwnerId, bookingState, from, size);

        // then
        assertThat(targetBookingDtos, hasSize(sourceBookingDtos.size()));
        for (BookingDto sourceBookingDto : sourceBookingDtos) {
            assertThat(targetBookingDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBookingDto.getStart())),
                    hasProperty("end", equalTo(sourceBookingDto.getEnd())),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))
            ));
        }
    }

    @Test
    void testGetAllBookingsByOwner_whenBookingState_REJECTED() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        List<BookingDto> sourceBookingDtos = List.of(
                makeBookingDto(itemId, starDate.toString(), endDate.toString()),
                makeBookingDto(itemId, starDate.plusDays(1).toString(), endDate.plusDays(1).toString()),
                makeBookingDto(itemId, starDate.plusDays(2).toString(), endDate.plusDays(2).toString())
        );

        for (BookingDto bookingDto : sourceBookingDtos) {
            Booking bookingEntity = BookingMapper.toBooking(bookingDto, itemEntity, userBookerEntity);
            bookingEntity.setStatus(BookingStatus.REJECTED);
            em.persist(bookingEntity);
        }
        em.flush();

        BookingState bookingState = BookingState.REJECTED;
        Integer from = 0;
        Integer size = 10;

        // when
        Collection<BookingDto> targetBookingDtos = bookingService.getAllBookingsByOwner(userOwnerId, bookingState, from, size);

        // then
        assertThat(targetBookingDtos, hasSize(sourceBookingDtos.size()));
        for (BookingDto sourceBookingDto : sourceBookingDtos) {
            assertThat(targetBookingDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBookingDto.getStart())),
                    hasProperty("end", equalTo(sourceBookingDto.getEnd())),
                    hasProperty("status", equalTo(BookingStatus.REJECTED)))
            ));
        }
    }

    @Test
    void testGetAllBookingsByOwner_whenBookingState_ALL() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        List<BookingDto> sourceBookingDtos = List.of(
                makeBookingDto(itemId, starDate.toString(), endDate.toString()),
                makeBookingDto(itemId, starDate.plusDays(1).toString(), endDate.plusDays(1).toString()),
                makeBookingDto(itemId, starDate.plusDays(2).toString(), endDate.plusDays(2).toString())
        );

        for (BookingDto bookingDto : sourceBookingDtos) {
            Booking bookingEntity = BookingMapper.toBooking(bookingDto, itemEntity, userBookerEntity);
            bookingEntity.setStatus(BookingStatus.WAITING);
            em.persist(bookingEntity);
        }
        em.flush();

        BookingState bookingState = BookingState.ALL;
        Integer from = 0;
        Integer size = 10;

        // when
        Collection<BookingDto> targetBookingDtos = bookingService.getAllBookingsByOwner(userOwnerId, bookingState, from, size);

        // then
        assertThat(targetBookingDtos, hasSize(sourceBookingDtos.size()));
        for (BookingDto sourceBookingDto : sourceBookingDtos) {
            assertThat(targetBookingDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBookingDto.getStart())),
                    hasProperty("end", equalTo(sourceBookingDto.getEnd())),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))
            ));
        }
    }

    @Test
    void testGetAllBookingsByOwner_whenBookingState_Current() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        List<BookingDto> sourceBookingDtos = List.of(
                makeBookingDto(itemId, starDate.plusDays(-3).toString(), endDate.plusDays(1).toString()),
                makeBookingDto(itemId, starDate.plusDays(-1).toString(), endDate.plusDays(1).toString()),
                makeBookingDto(itemId, starDate.plusDays(-2).toString(), endDate.plusDays(2).toString())
        );

        for (BookingDto bookingDto : sourceBookingDtos) {
            Booking bookingEntity = BookingMapper.toBooking(bookingDto, itemEntity, userBookerEntity);
            bookingEntity.setStatus(BookingStatus.WAITING);
            em.persist(bookingEntity);
        }
        em.flush();

        BookingState bookingState = BookingState.CURRENT;
        Integer from = 0;
        Integer size = 10;

        // when
        Collection<BookingDto> targetBookingDtos = bookingService.getAllBookingsByOwner(userOwnerId, bookingState, from, size);

        // then
        assertThat(targetBookingDtos, hasSize(sourceBookingDtos.size()));
        for (BookingDto sourceBookingDto : sourceBookingDtos) {
            assertThat(targetBookingDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBookingDto.getStart())),
                    hasProperty("end", equalTo(sourceBookingDto.getEnd())),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))
            ));
        }
    }


    @Test
    void testGetAllBookingsByOwner_whenBookingState_FUTURE() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserOwnerDto = makeUserDto("ivan@email", "Ivan");
        UserDto sourceUserBookerDto = makeUserDto("petr@email", "Petr");
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);

        User userOwnerEntity = UserMapper.toUser(sourceUserOwnerDto);
        em.persist(userOwnerEntity);
        em.flush();
        Long userOwnerId = userOwnerEntity.getId();

        User userBookerEntity = UserMapper.toUser(sourceUserBookerDto);
        em.persist(userBookerEntity);
        em.flush();
        Long userBookerId = userBookerEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userOwnerId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        List<BookingDto> sourceBookingDtos = List.of(
                makeBookingDto(itemId, starDate.plusDays(1).toString(), endDate.plusDays(2).toString()),
                makeBookingDto(itemId, starDate.plusDays(2).toString(), endDate.plusDays(3).toString()),
                makeBookingDto(itemId, starDate.plusDays(3).toString(), endDate.plusDays(4).toString())
        );

        for (BookingDto bookingDto : sourceBookingDtos) {
            Booking bookingEntity = BookingMapper.toBooking(bookingDto, itemEntity, userBookerEntity);
            bookingEntity.setStatus(BookingStatus.WAITING);
            em.persist(bookingEntity);
        }
        em.flush();

        BookingState bookingState = BookingState.FUTURE;
        Integer from = 0;
        Integer size = 10;

        // when
        Collection<BookingDto> targetBookingDtos = bookingService.getAllBookingsByOwner(userOwnerId, bookingState, from, size);

        // then
        assertThat(targetBookingDtos, hasSize(sourceBookingDtos.size()));
        for (BookingDto sourceBookingDto : sourceBookingDtos) {
            assertThat(targetBookingDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(sourceBookingDto.getStart())),
                    hasProperty("end", equalTo(sourceBookingDto.getEnd())),
                    hasProperty("status", equalTo(BookingStatus.WAITING)))
            ));
        }
    }


    @Test
    void testGetAllBookingsByOwner_whenNoBookings() {
        // given
        Long userOwnerId = 1L;
        BookingState bookingState = BookingState.PAST;
        Integer from = 0;
        Integer size = 10;

        // when & then
        assertThatThrownBy(() -> bookingService.getAllBookingsByOwner(userOwnerId, bookingState, from, size))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Пользователя с ID = " + userOwnerId + " не является владельцем вещей");
    }


    private ItemDto makeItemDto(String name, String description, boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private UserDto makeUserDto(String email, String name) {
        return UserDto.builder()
                .email(email)
                .name(name)
                .build();
    }

    private BookingDto makeBookingDto(Long itemId, String start, String end) {
        return BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
    }
}