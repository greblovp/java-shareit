package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;


@Data
@Builder
public class BookingForItemDto {
    private Long id;
    private String start;
    private String end;
    private BookingStatus status;
    private Long bookerId;
}
