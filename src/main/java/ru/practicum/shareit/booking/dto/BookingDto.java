package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class BookingDto {
    private Long id;
    @NotNull
    private Long itemId;
    @NotNull
    private String start;
    @NotNull
    private String end;
    private BookingStatus status;
    private ItemDto item;
    private UserDto booker;
}
