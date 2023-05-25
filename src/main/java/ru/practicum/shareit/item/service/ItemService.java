package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto patchItem(Long userId, long itemId, ItemDto itemDto);

    Collection<ItemDto> getItems(long userId);

    ItemDto getItemById(long itemId);

    Collection<ItemDto> searchItem(String text);
}
