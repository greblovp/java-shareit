package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {
    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void testCreateValidItem() {
        Long userId = 1L;
        ItemDto itemToCreate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.createItem(userId, itemToCreate)).thenReturn(itemToCreate);

        String response = mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToCreate)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).createItem(userId, itemToCreate);
        assertEquals(objectMapper.writeValueAsString(itemToCreate), response);
    }

    @SneakyThrows
    @Test
    void testCreateItemWithInvalidHeader() {
        Long userId = 1L;
        ItemDto itemToCreate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.createItem(userId, itemToCreate)).thenReturn(itemToCreate);

        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemToCreate)))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).createItem(userId, itemToCreate);
    }

    @SneakyThrows
    @Test
    void testUpdateValidItem() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemToUpdate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.patchItem(userId, itemId, itemToUpdate)).thenReturn(itemToUpdate);

        String response = mockMvc.perform(patch("/items/" + itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).patchItem(userId, itemId, itemToUpdate);
        assertEquals(objectMapper.writeValueAsString(itemToUpdate), response);
    }

    @SneakyThrows
    @Test
    void testGetItems() {
        Long userId = 1L;
        ItemOwnerDto item = ItemOwnerDto.builder()
                .name("name")
                .build();
        when(itemService.getItems(userId)).thenReturn(List.of(item));

        String response = mockMvc.perform(get("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).getItems(userId);
        assertEquals(objectMapper.writeValueAsString(List.of(item)), response);
    }


    @Test
    @SneakyThrows
    public void testFindById() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemOwnerDto item = ItemOwnerDto.builder()
                .name("name")
                .build();
        when(itemService.getItemById(userId, itemId)).thenReturn(item);

        String response = mockMvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService).getItemById(userId, itemId);
        assertEquals(objectMapper.writeValueAsString(item), response);
    }

    @Test
    @SneakyThrows
    public void testSearchItem() {
        ItemDto item = ItemDto.builder()
                .name("name")
                .build();

        when(itemService.searchItem("name")).thenReturn(List.of(item));

        String response = mockMvc.perform(get("/items/search")
                        .contentType("application/json")
                        .param("text", "name"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).searchItem("name");
        assertEquals(objectMapper.writeValueAsString(List.of(item)), response);

    }
}