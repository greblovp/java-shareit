package ru.practicum.shareit.item.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.ItemValidationException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Qualifier("inMemoryItemRepository")
    @NonNull
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        //Проверяем, что пользователь существует
        userService.getUserById(userId);
        //Для создания Вещи необходимо проверить заполненность полей
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null
                || itemDto.getName().isBlank() || itemDto.getDescription().isBlank()) {
            throw new ItemValidationException("Заполните все поля.");
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(userId);
        return ItemMapper.toItemDto(itemRepository.createItem(item));
    }

    @Override
    public ItemDto patchItem(Long userId, long itemId, ItemDto itemDto) {
        //Проверяем, что пользователь существует
        userService.getUserById(userId);
        Item oldItem = checkItemId(itemId);
        if (oldItem.getOwnerId() != userId) {
            throw new WrongItemOwnerException("У вещи с ID = " + itemId + " другой владелец.");
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setId(itemId);
        return ItemMapper.toItemDto(itemRepository.patchItem(item));
    }

    @Override
    public Collection<ItemDto> getItems(long userId) {
        return itemRepository.getItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return ItemMapper.toItemDto(checkItemId(itemId));
    }

    @Override
    public Collection<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchItem(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private Item checkItemId(long id) {
        return itemRepository.getItemById(id).orElseThrow(()
                -> new ItemNotFoundException("Вещь с ID = " + id + " не найдена."));
    }
}
