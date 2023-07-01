package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BookingValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplUnitTest {
    @MockBean
    private final BookingRepository bookingRepository;
    @MockBean
    private final UserService userService;
    @MockBean
    private final ItemRepository itemRepository;

    private BookingServiceImpl bookingService;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userService, itemRepository);
    }

    @Test
    void createBooking_whenEndBeforeStart() {
        // given
        Long itemId = 1L;
        Long userId = 1L;
        LocalDateTime starDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(-1);

        Item item = new Item();
        item.setOwnerId(userId + 1L);
        item.setIsAvailable(true);

        when(userService.getUserById(userId)).thenReturn(UserDto.builder().id(userId).build());
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(userId, sourceBookingDto))
                .isInstanceOf(BookingValidationException.class)
                .hasMessage("Дата оконачания бронирования не может быть раньше даты начала");
    }

    @Test
    void createBooking_whenStartBeforeNow() {
        // given
        Long itemId = 1L;
        Long userId = 1L;
        LocalDateTime starDate = LocalDateTime.now().plusDays(-1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Item item = new Item();
        item.setOwnerId(userId + 1L);
        item.setIsAvailable(true);

        when(userService.getUserById(userId)).thenReturn(UserDto.builder().id(userId).build());
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(userId, sourceBookingDto))
                .isInstanceOf(BookingValidationException.class)
                .hasMessage("Дата начала бронирования не может быть раньше текущей даты");
    }

    @Test
    void createBooking_whenEndBeforeNow() {
        // given
        Long itemId = 1L;
        Long userId = 1L;
        LocalDateTime starDate = LocalDateTime.now().plusDays(-5);
        LocalDateTime endDate = LocalDateTime.now().plusDays(-1);

        Item item = new Item();
        item.setOwnerId(userId + 1L);
        item.setIsAvailable(true);

        when(userService.getUserById(userId)).thenReturn(UserDto.builder().id(userId).build());
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        BookingDto sourceBookingDto = makeBookingDto(itemId, starDate.toString(), endDate.toString());

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(userId, sourceBookingDto))
                .isInstanceOf(BookingValidationException.class)
                .hasMessage("Дата оконачания бронирования не может быть раньше текущей даты");
    }

    @Test
    void createBooking_whenStartEqualsEnd() {
        // given
        Long itemId = 1L;
        Long userId = 1L;
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Item item = new Item();
        item.setOwnerId(userId + 1L);
        item.setIsAvailable(true);

        when(userService.getUserById(userId)).thenReturn(UserDto.builder().id(userId).build());
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        BookingDto sourceBookingDto = makeBookingDto(itemId, endDate.toString(), endDate.toString());

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(userId, sourceBookingDto))
                .isInstanceOf(BookingValidationException.class)
                .hasMessage("Дата начала и оконачания бронирования не могут совпадать");
    }

    private BookingDto makeBookingDto(Long itemId, String start, String end) {
        return BookingDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
    }
}