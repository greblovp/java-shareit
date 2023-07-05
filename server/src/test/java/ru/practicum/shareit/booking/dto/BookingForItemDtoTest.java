package ru.practicum.shareit.booking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingStatus;


public class BookingForItemDtoTest {

    @Test
    void createBookingForItemDto() {
        BookingForItemDto dto = BookingForItemDto.builder()
                .id(1L)
                .start("2022-01-01")
                .end("2022-01-05")
                .status(BookingStatus.WAITING)
                .bookerId(2L)
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo("2022-01-01");
        assertThat(dto.getEnd()).isEqualTo("2022-01-05");
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBookerId()).isEqualTo(2L);
    }

    @Test
    void shouldPassValidationWhenAllFieldsAreSet() {
        BookingForItemDto dto = BookingForItemDto.builder()
                .id(1L)
                .start("2021-12-31")
                .end("2022-01-01")
                .status(BookingStatus.WAITING)
                .bookerId(123L)
                .build();


    }
}