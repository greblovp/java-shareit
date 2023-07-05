package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {

//    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
//    private final Validator validator = validatorFactory.getValidator();
//
//    @Test
//    public void testNotNullConstraints() {
//        BookingDto bookingDto = BookingDto.builder()
//                .id(1L)
//                .itemId(null)
//                .start(null)
//                .end(null)
//                .status(BookingStatus.WAITING)
//                .build();
//
//        assertEquals(3, validator.validate(bookingDto).size());
//    }

    @Test
    void testBookingDtoAllFields() {
        ItemDto item = ItemDto.builder().id(1L).name("Item 1").build();
        UserDto user = UserDto.builder().id(1L).name("user1").build();

        BookingDto booking = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start("2021-01-01")
                .end("2021-01-07")
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(user)
                .build();

        assertEquals(1L, booking.getId());
        assertEquals(1L, booking.getItemId());
        assertEquals("2021-01-01", booking.getStart());
        assertEquals("2021-01-07", booking.getEnd());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(item, booking.getItem());
        assertEquals(user, booking.getBooker());
    }
}