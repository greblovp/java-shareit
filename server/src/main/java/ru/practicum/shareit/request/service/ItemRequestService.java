package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);

    Collection<ItemRequestDto> getItemRequests(Long userId);

    Collection<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size);
}
