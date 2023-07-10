package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public static List<ItemDto> toItemDto(Iterable<Item> items) {
        List<ItemDto> dtos = new ArrayList<>();
        for (Item item : items) {
            dtos.add(toItemDto(item));
        }
        return dtos;
    }

    public static ItemExtendedDto toItemOwnerDto(Item item) {
        return ItemExtendedDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .comments(new ArrayList<>())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setIsAvailable(itemDto.getAvailable());
        item.setRequestId(itemDto.getRequestId());
        return item;
    }
}
