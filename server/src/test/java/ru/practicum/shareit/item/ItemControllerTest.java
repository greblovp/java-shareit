package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    void testCreateValidItem() throws Exception {
        // Given
        Long userId = 1L;
        ItemDto itemToCreate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.createItem(userId, itemToCreate)).thenReturn(itemToCreate);

        // when
        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("name"));
    }

    @Test
    void testCreateItemWithInvalidHeader() throws Exception {
        // Given
        Long userId = 1L;
        ItemDto itemToCreate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.createItem(userId, itemToCreate)).thenReturn(itemToCreate);

        // when
        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemToCreate)))
                .andExpect(status().isInternalServerError());

        // then
        verify(itemService, never()).createItem(userId, itemToCreate);
    }

    @Test
    void testUpdateValidItem() throws Exception {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemToUpdate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.patchItem(userId, itemId, itemToUpdate)).thenReturn(itemToUpdate);

        // when
        mockMvc.perform(patch("/items/" + itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"));
    }

    @Test
    void testUpdate_whenWrongOwner() throws Exception {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemToUpdate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.patchItem(userId, itemId, itemToUpdate)).thenThrow(new WrongItemOwnerException("error"));

        // when
        mockMvc.perform(patch("/items/" + itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetItems() throws Exception {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;
        ItemExtendedDto item = ItemExtendedDto.builder()
                .name("name")
                .build();
        when(itemService.getItems(userId, from, size)).thenReturn(List.of(item));

        // when
        mockMvc.perform(get("/items")
                        .contentType("application/json")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("name"));
    }


    @Test
    public void testFindById() throws Exception {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        ItemExtendedDto item = ItemExtendedDto.builder()
                .name("name")
                .build();
        when(itemService.getItemById(userId, itemId)).thenReturn(item);

        // when
        mockMvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"));
    }

    @Test
    public void testFindByIdNotFound() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;

        when(itemService.getItemById(userId, itemId)).thenThrow(new ItemNotFoundException("error"));

        mockMvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSearchItem() throws Exception {
        // Given
        ItemDto item = ItemDto.builder()
                .name("name")
                .build();
        Integer from = 0;
        Integer size = 10;

        when(itemService.searchItem("name", from, size)).thenReturn(List.of(item));

        // when
        mockMvc.perform(get("/items/search")
                        .contentType("application/json")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("text", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("name"));

    }

    @Test
    public void testAddCommentSuccess() throws Exception {
        // Given
        CommentDto commentDto = CommentDto.builder().text("Test comment").build();
        long userId = 1L;
        long itemId = 1L;

        when(itemService.addComment(userId, itemId, commentDto)).thenReturn(commentDto);

        // when
        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test comment"));
    }
}