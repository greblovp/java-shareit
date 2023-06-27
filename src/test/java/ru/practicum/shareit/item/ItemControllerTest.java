package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        // Given
        Long userId = 1L;
        ItemDto itemToCreate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.createItem(userId, itemToCreate)).thenReturn(itemToCreate);

        // when
        String response = mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToCreate)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(itemService).createItem(userId, itemToCreate);
        assertEquals(objectMapper.writeValueAsString(itemToCreate), response);
    }

    @SneakyThrows
    @Test
    void testCreateItemWithInvalidHeader() {
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

    @SneakyThrows
    @Test
    void testUpdateValidItem() {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemToUpdate = ItemDto.builder()
                .name("name")
                .build();
        when(itemService.patchItem(userId, itemId, itemToUpdate)).thenReturn(itemToUpdate);

        // when
        String response = mockMvc.perform(patch("/items/" + itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(itemService).patchItem(userId, itemId, itemToUpdate);
        assertEquals(objectMapper.writeValueAsString(itemToUpdate), response);
    }

    @SneakyThrows
    @Test
    void testGetItems() {
        // Given
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;
        ItemExtendedDto item = ItemExtendedDto.builder()
                .name("name")
                .build();
        when(itemService.getItems(userId, from, size)).thenReturn(List.of(item));

        // when
        String response = mockMvc.perform(get("/items")
                        .contentType("application/json")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(itemService).getItems(userId, from, size);
        assertEquals(objectMapper.writeValueAsString(List.of(item)), response);
    }


    @Test
    @SneakyThrows
    public void testFindById() {
        // Given
        Long userId = 1L;
        Long itemId = 1L;
        ItemExtendedDto item = ItemExtendedDto.builder()
                .name("name")
                .build();
        when(itemService.getItemById(userId, itemId)).thenReturn(item);

        // when
        String response = mockMvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(itemService).getItemById(userId, itemId);
        assertEquals(objectMapper.writeValueAsString(item), response);
    }

    @Test
    @SneakyThrows
    public void testSearchItem() {
        // Given
        ItemDto item = ItemDto.builder()
                .name("name")
                .build();
        Integer from = 0;
        Integer size = 10;

        when(itemService.searchItem("name", from, size)).thenReturn(List.of(item));

        // when
        String response = mockMvc.perform(get("/items/search")
                        .contentType("application/json")
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("text", "name"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(itemService).searchItem("name", from, size);
        assertEquals(objectMapper.writeValueAsString(List.of(item)), response);

    }

    @Test
    @SneakyThrows
    public void testAddCommentSuccess() {
        // Given
        CommentDto commentDto = CommentDto.builder().text("Test comment").build();
        Long userId = 1L;
        Long itemId = 1L;

        when(itemService.addComment(userId, itemId, commentDto)).thenReturn(commentDto);

        // when
        String response = mockMvc.perform(post("/items/" + itemId + "/comment")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        verify(itemService).addComment(userId, itemId, commentDto);
        assertEquals(objectMapper.writeValueAsString(commentDto), response);
    }

    @Test
    @SneakyThrows
    public void testAddCommentInvalidRequestBody() {
        // Given
        CommentDto commentDto = CommentDto.builder().text("").build();
        Long userId = 1L;
        Long itemId = 1L;

        when(itemService.addComment(userId, itemId, commentDto)).thenReturn(commentDto);

        // when
        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        //then
        verify(itemService, never()).addComment(anyLong(), anyLong(), any());
    }
}