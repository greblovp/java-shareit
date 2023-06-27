package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;


    @Test
    @SneakyThrows
    public void testCreateItemRequest() {
        // Given
        Long userId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("description").build();
        when(itemRequestService.createItemRequest(userId, itemRequestDto)).thenReturn(itemRequestDto);

        // When
        String response = mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        verify(itemRequestService).createItemRequest(userId, itemRequestDto);
        assertEquals(objectMapper.writeValueAsString(itemRequestDto), response);
    }

    @Test
    @SneakyThrows
    public void testCreateItemRequest_shouldThrowBookingValidationException() {
        // Given
        Long userId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        when(itemRequestService.createItemRequest(userId, itemRequestDto)).thenReturn(itemRequestDto);

        // When
        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());

        // Then
        verify(itemRequestService, never()).createItemRequest(anyLong(), any());
    }

    @Test
    @SneakyThrows
    public void testGetItemRequests() {
        // Given
        Long userId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("description").build();
        when(itemRequestService.getItemRequests(userId)).thenReturn(List.of(itemRequestDto));

        // When
        String response = mockMvc.perform(get("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        verify(itemRequestService).getItemRequests(userId);
        assertEquals(objectMapper.writeValueAsString(List.of(itemRequestDto)), response);
    }

    @Test
    @SneakyThrows
    public void testGetItemRequestById() {
        // Given
        Long userId = 1L;
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("description").build();
        when(itemRequestService.getItemRequestById(userId, requestId)).thenReturn(itemRequestDto);

        // When
        String response = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        verify(itemRequestService).getItemRequestById(userId, requestId);
        assertEquals(objectMapper.writeValueAsString(itemRequestDto), response);
    }

    @Test
    public void testGetAllItemRequests() throws Exception {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("description").build();
        when(itemRequestService.getAllItemRequests(userId, from, size)).thenReturn(List.of(itemRequestDto));

        // When
        String response = mockMvc.perform(get("/requests/all")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        verify(itemRequestService).getAllItemRequests(userId, from, size);
        assertEquals(objectMapper.writeValueAsString(List.of(itemRequestDto)), response);
    }
}