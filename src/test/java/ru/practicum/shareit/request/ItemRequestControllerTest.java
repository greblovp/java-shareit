package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    public void testCreateItemRequest() throws Exception {
        // Given
        Long userId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("description").build();
        when(itemRequestService.createItemRequest(userId, itemRequestDto)).thenReturn(itemRequestDto);

        // When
        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("description"));
    }

    @Test
    public void testCreateItemRequest_shouldThrowBookingValidationException() throws Exception {
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
    public void testGetItemRequests() throws Exception {
        // Given
        Long userId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("description").build();
        when(itemRequestService.getItemRequests(userId)).thenReturn(List.of(itemRequestDto));

        // When
        mockMvc.perform(get("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("description"));
    }

    @Test
    public void testGetItemRequestById() throws Exception {
        // Given
        Long userId = 1L;
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("description").build();
        when(itemRequestService.getItemRequestById(userId, requestId)).thenReturn(itemRequestDto);

        // When
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("description"));
    }

    @Test
    public void testFindByIdNotFound() throws Exception {
        Long userId = 1L;
        Long requestId = 1L;

        when(itemRequestService.getItemRequestById(userId, requestId)).thenThrow(new ItemRequestNotFoundException("error"));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
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
        mockMvc.perform(get("/requests/all")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("description"));
    }
}