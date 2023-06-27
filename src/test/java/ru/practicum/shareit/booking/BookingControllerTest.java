package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @SneakyThrows
    public void testCreateBooking() {
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
        String response = mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(bookingService).createBooking(userId, bookingDto);
        assertEquals(objectMapper.writeValueAsString(bookingDto), response);
    }

    @Test
    @SneakyThrows
    public void testCreateBooking_whenStartDateIsMissing() {
        // given
        Long itemId = 1L;
        Long userId = 2L;
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .end(endDate.toString())
                .build();

        when(bookingService.createBooking(userId, bookingDto)).thenReturn(bookingDto);

        // when
        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        //then
        verify(bookingService, never()).createBooking(anyLong(), any());

    }

    @Test
    @SneakyThrows
    public void testGetBooking() {
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
        String response = mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(bookingService).getBooking(userId, bookingId);
        assertEquals(objectMapper.writeValueAsString(bookingDto), response);
    }

    @Test
    @SneakyThrows
    public void testApproveBooking() {
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

        String response = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(bookingService).approveBooking(userId, bookingId, true);
        assertEquals(objectMapper.writeValueAsString(bookingDto), response);
    }

    @Test
    @SneakyThrows
    public void testGetAllBookings() {
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
        String response = mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("state", state.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        verify(bookingService).getAllBookings(userId, state, from, size);
        assertEquals(objectMapper.writeValueAsString(List.of(bookingDto)), response);
    }

    @Test
    @SneakyThrows
    public void testGetAllBookings_whenUnknownBookingState() {
        // given
        Long userId = 2L;
        Integer from = 0;
        Integer size = 10;

        // when
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("state", "UNKNOWN")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        //then
        verify(bookingService, never()).getAllBookings(anyLong(), any(), anyInt(), anyInt());
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
        String response = mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("state", state.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        verify(bookingService).getAllBookingsByOwner(userId, state, from, size);
        assertEquals(objectMapper.writeValueAsString(List.of(bookingDto)), response);
    }
}