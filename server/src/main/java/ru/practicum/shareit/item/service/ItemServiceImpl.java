package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.exception.CommentNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.PageGetter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
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

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemExtendedDto> getItems(Long userId, Integer from, Integer size) {

        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("id").ascending());

        return itemRepository.findByOwnerId(userId, page).getContent().stream()
                .map(this::getItemLastAndNextBooking)
                .map(this::getComments)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public ItemExtendedDto getItemById(Long userId, Long itemId) {
        Item itemToGet = checkItemId(itemId);
        ItemExtendedDto itemExtendedDto;
        if (userId.equals(itemToGet.getOwnerId())) {
            itemExtendedDto = getItemLastAndNextBooking(itemToGet);
        } else {
            itemExtendedDto = ItemMapper.toItemOwnerDto(itemToGet);
        }
        return getComments(itemExtendedDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> searchItem(String text, Integer from, Integer size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        Pageable page = PageGetter.getPageRequest(from, size, Sort.unsorted());

        return itemRepository.search(text, page).getContent().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(Long userId, long itemId, CommentDto commentDto) {
        UserDto userDto = userService.getUserById(userId);
        Item item = checkItemId(itemId);

        Collection<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndEndDateLessThanAndStatus(itemId, userId,
                LocalDateTime.now(), BookingStatus.APPROVED);
        if (bookings.isEmpty()) {
            throw new CommentNotAvailableException("У пользователя с ID = " + userId + " нет завершенных бронирований вещи с ID = " + itemId);
        }

        Comment savedComment = commentRepository.save(CommentMapper.toComment(commentDto, item, UserMapper.toUser(userDto)));

        return CommentMapper.toCommentDto(savedComment);
    }

    private Item checkItemId(Long id) {
        return itemRepository.findById(id).orElseThrow(()
                -> new ItemNotFoundException("Вещь с ID = " + id + " не найдена."));
    }

    private ItemExtendedDto getItemLastAndNextBooking(Item item) {
        ItemExtendedDto itemExtendedDto = ItemMapper.toItemOwnerDto(item);

        Optional<Booking> lastBookingOptional = Optional.ofNullable(
                bookingRepository.findFirst1ByItemIdAndStartDateLessThanEqualAndStatusOrderByStartDateDesc(item.getId(), LocalDateTime.now(), BookingStatus.APPROVED));
        Optional<Booking> nextBookingOptional = Optional.ofNullable(
                bookingRepository.findFirst1ByItemIdAndStartDateGreaterThanAndStatusOrderByStartDate(item.getId(), LocalDateTime.now(), BookingStatus.APPROVED));

        lastBookingOptional.ifPresent(booking -> itemExtendedDto.setLastBooking(BookingMapper.toBookingForItemDto(booking)));
        nextBookingOptional.ifPresent(booking -> itemExtendedDto.setNextBooking(BookingMapper.toBookingForItemDto(booking)));

        return itemExtendedDto;
    }

    private ItemExtendedDto getComments(ItemExtendedDto dto) {
        commentRepository.findAllByItemIdOrderById(dto.getId())
                .forEach(comment -> dto.getComments().add(CommentMapper.toCommentDto(comment)));
        return dto;
    }
}
