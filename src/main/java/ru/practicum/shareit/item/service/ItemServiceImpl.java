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

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        //Для создания Вещи необходимо проверить заполненность полей
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null
                || itemDto.getName().isBlank() || itemDto.getDescription().isBlank()) {
            throw new ItemValidationException("Заполните все поля.");
        }

        //Проверяем, что пользователь существует
        userService.getUserById(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(userId);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto patchItem(Long userId, Long itemId, ItemDto itemDto) {
        //Проверяем, что пользователь существует
        userService.getUserById(userId);
        Item oldItem = checkItemId(itemId);
        if (!userId.equals(oldItem.getOwnerId())) {
            throw new WrongItemOwnerException("У вещи с ID = " + itemId + " другой владелец.");
        }

        Item itemToUpdate = checkItemId(itemId);
        if (itemDto.getName() != null) {
            itemToUpdate.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            itemToUpdate.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            itemToUpdate.setIsAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(itemToUpdate));
    }

    @Override
    public Collection<ItemDto> getItems(Long userId) {
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return ItemMapper.toItemDto(checkItemId(itemId));
    }

    @Override
    public Collection<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private Item checkItemId(Long id) {
        return itemRepository.findById(id).orElseThrow(()
                -> new ItemNotFoundException("Вещь с ID = " + id + " не найдена."));
    }
}
