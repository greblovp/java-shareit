package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.PageGetter;

import java.util.Collection;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        //Проверяем, что пользователь существует
        User requestor = UserMapper.toUser(userService.getUserById(userId));

        ItemRequest itemRequestToCreate = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);

        ItemRequest createdItemRequest = itemRequestRepository.save(itemRequestToCreate);

        return ItemRequestMapper.toItemRequestDto(createdItemRequest);
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        //Проверяем, что пользователь существует
        userService.getUserById(userId);

        ItemRequest itemRequest = checkItemRequestId(requestId);

        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

        Collection<Item> items = itemRepository.findByRequestId(requestId);

        itemRequestDto.setItems(ItemMapper.toItemDto(items));

        return itemRequestDto;
    }

    @Override
    public Collection<ItemRequestDto> getItemRequests(Long userId) {
        //Проверяем, что пользователь существует
        userService.getUserById(userId);

        Collection<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDateDesc(userId);
        Collection<ItemRequestDto> itemRequestDtos = ItemRequestMapper.toItemRequestDto(itemRequests);

        for (ItemRequestDto itemRequestDto : itemRequestDtos) {
            Collection<Item> items = itemRepository.findByRequestId(itemRequestDto.getId());
            itemRequestDto.setItems(ItemMapper.toItemDto(items));
        }

        return itemRequestDtos;
    }

    @Override
    public Collection<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        //Проверяем, что пользователь существует
        userService.getUserById(userId);

        Pageable page = PageGetter.getPageRequest(from, size,  Sort.by("createdDate").descending());

        Collection<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdNot(userId, page).getContent();

        Collection<ItemRequestDto> itemRequestDtos = ItemRequestMapper.toItemRequestDto(itemRequests);

        for (ItemRequestDto itemRequestDto : itemRequestDtos) {
            Collection<Item> items = itemRepository.findByRequestId(itemRequestDto.getId());
            itemRequestDto.setItems(ItemMapper.toItemDto(items));
        }

        return itemRequestDtos;
    }

    private ItemRequest checkItemRequestId(Long id) {
        return itemRequestRepository.findById(id).orElseThrow(()
                -> new ItemRequestNotFoundException("Запрос на  вещь с ID = " + id + " не найден."));
    }
}
