package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.exception.ItemNotAvailableException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateBooking() throws Exception {
        // given
        Long itemId = 1L;
        Long userId = 2L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(startDate.toString())
                .end(endDate.toString())
                .build();

        when(bookingService.createBooking(userId, bookingDto)).thenReturn(bookingDto);

        // when
        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.start").value(startDate.toString()))
                .andExpect(jsonPath("$.end").value(endDate.toString()));
    }

    @Test
    public void testCreateBooking_whenItemNotAvailable() throws Exception {
        // given
        Long itemId = 1L;
        Long userId = 2L;
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .end(endDate.toString())
                .build();

        when(bookingService.createBooking(userId, bookingDto)).thenThrow(new ItemNotAvailableException("error"));

        // when
        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetBooking() throws Exception {
        // given
        Long itemId = 1L;
        Long bookingId = 3L;
        Long userId = 2L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(startDate.toString())
                .end(endDate.toString())
                .build();

        when(bookingService.getBooking(userId, bookingId)).thenReturn(bookingDto);

        // when
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.start").value(startDate.toString()))
                .andExpect(jsonPath("$.end").value(endDate.toString()));
    }

    @Test
    public void testGetBooking_whenNotFound() throws Exception {
        // given
        Long itemId = 1L;
        Long bookingId = 3L;
        Long userId = 2L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(startDate.toString())
                .end(endDate.toString())
                .build();

        when(bookingService.getBooking(userId, bookingId)).thenThrow(new BookingNotFoundException("error"));

        // when
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testApproveBooking() throws Exception {
        // given
        Long itemId = 1L;
        Long bookingId = 3L;
        Long userId = 2L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(startDate.toString())
                .end(endDate.toString())
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approveBooking(userId, bookingId, true)).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.start").value(startDate.toString()))
                .andExpect(jsonPath("$.end").value(endDate.toString()))
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.name()));
    }

    @Test
    public void testGetAllBookings() throws Exception {
        // given
        Long itemId = 1L;
        Long userId = 2L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(startDate.toString())
                .end(endDate.toString())
                .status(BookingStatus.APPROVED)
                .build();
        BookingState state = BookingState.FUTURE;
        Integer from = 0;
        Integer size = 10;

        when(bookingService.getAllBookings(userId, state, from, size)).thenReturn(List.of(bookingDto));

        // when
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("state", state.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].itemId").value(itemId))
                .andExpect(jsonPath("$.[0].start").value(startDate.toString()))
                .andExpect(jsonPath("$.[0].end").value(endDate.toString()));
    }

    @Test
    public void testGetAllBookingsByOwner() throws Exception {
        // given
        Long itemId = 1L;
        Long userId = 2L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(startDate.toString())
                .end(endDate.toString())
                .status(BookingStatus.APPROVED)
                .build();
        BookingState state = BookingState.FUTURE;
        Integer from = 0;
        Integer size = 10;

        when(bookingService.getAllBookingsByOwner(userId, state, from, size)).thenReturn(List.of(bookingDto));

        // when
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("state", state.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].itemId").value(itemId))
                .andExpect(jsonPath("$.[0].start").value(startDate.toString()))
                .andExpect(jsonPath("$.[0].end").value(endDate.toString()));
    }
}