package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ItemDtoTest {

    @Test
    public void testBuilder() {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("test item")
                .description("this is a test item")
                .available(true)
                .requestId(123L)
                .build();

        Assertions.assertEquals(1L, item.getId());
        Assertions.assertEquals("test item", item.getName());
        Assertions.assertEquals("this is a test item", item.getDescription());
        Assertions.assertTrue(item.getAvailable());
        Assertions.assertEquals(123L, item.getRequestId());
    }

    @Test
    public void testGettersAndSetters() {
        ItemDto item = ItemDto.builder().build();
        item.setId(1L);
        item.setName("test item");
        item.setDescription("this is a test item");
        item.setAvailable(true);
        item.setRequestId(123L);

        Assertions.assertEquals(1L, item.getId());
        Assertions.assertEquals("test item", item.getName());
        Assertions.assertEquals("this is a test item", item.getDescription());
        Assertions.assertTrue(item.getAvailable());
        Assertions.assertEquals(123L, item.getRequestId());
    }

}