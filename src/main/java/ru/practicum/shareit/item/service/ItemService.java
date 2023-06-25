package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;

import java.util.Collection;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto patchItem(Long userId, Long itemId, ItemDto itemDto);

    Collection<ItemExtendedDto> getItems(Long userId);

    ItemExtendedDto getItemById(Long userId, Long itemId);

    Collection<ItemDto> searchItem(String text);

    CommentDto addComment(Long userId, long itemId, CommentDto commentDto);
}
