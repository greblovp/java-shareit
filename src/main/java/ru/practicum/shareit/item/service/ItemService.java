package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto patchItem(Long userId, Long itemId, ItemDto itemDto);

    Collection<ItemDto> getItems(Long userId);

    ItemDto getItemById(Long itemId);

    Collection<ItemDto> searchItem(String text);
}
