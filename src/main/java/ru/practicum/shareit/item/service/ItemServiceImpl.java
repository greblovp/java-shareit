package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.ItemValidationException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Transactional
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

    @Transactional
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
    public Collection<ItemOwnerDto> getItems(Long userId) {
        return itemRepository.findByOwnerIdOrderById(userId).stream()
                .map(this::getItemLastAndNextBooking)
                .collect(Collectors.toList());
    }

    @Override
    public ItemOwnerDto getItemById(Long userId, Long itemId) {
        Item itemToGet = checkItemId(itemId);
        if (userId.equals(itemToGet.getOwnerId())) {
            return getItemLastAndNextBooking(itemToGet);
        }
        return ItemMapper.toItemOwnerDto(itemToGet);
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

    private ItemOwnerDto getItemLastAndNextBooking(Item item) {
        ItemOwnerDto itemOwnerDto = ItemMapper.toItemOwnerDto(item);

        Optional<Booking> lastBookingOptional = Optional.ofNullable(
                bookingRepository.findFirst1ByItemIdAndStartDateLessThanEqualOrderByStartDateDesc(item.getId(), LocalDateTime.now()));
        Optional<Booking> nextBookingOptional = Optional.ofNullable(
                bookingRepository.findFirst1ByItemIdAndStartDateGreaterThanOrderByStartDate(item.getId(), LocalDateTime.now()));

        lastBookingOptional.ifPresent(booking -> itemOwnerDto.setLastBooking(BookingMapper.toBookingForItemDto(booking)));
        nextBookingOptional.ifPresent(booking -> itemOwnerDto.setNextBooking(BookingMapper.toBookingForItemDto(booking)));

        return itemOwnerDto;
    }
}
