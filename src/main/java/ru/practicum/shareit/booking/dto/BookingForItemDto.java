package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class BookingForItemDto {
    private Long id;
    @NotNull
    private String start;
    @NotNull
    private String end;
    private BookingStatus status;
    private Long bookerId;
}
