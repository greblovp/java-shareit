package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto patchItem(Long userId, Long itemId, ItemDto itemDto);

    Collection<ItemOwnerDto> getItems(Long userId);

    ItemOwnerDto getItemById(Long userId, Long itemId);

    Collection<ItemDto> searchItem(String text);
}
