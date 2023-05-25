package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto) {
        log.info("Создать вещь для пользователя ID = {}", userId);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto,
                             @PathVariable long itemId) {
        log.info("Обновить вещь ID = {} пользователя ID = {}", itemId, userId);
        return itemService.patchItem(userId, itemId, itemDto);
    }

    @GetMapping
    public Collection<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Вывести все вещи пользователя ID = {}", userId);
        return itemService.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable long itemId) {
        log.info("Вывести вещь ID = {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(@RequestParam String text) {
        log.info("Вывести вещи, содержащие в названии или описании текст {}", text);
        return itemService.searchItem(text);
    }

}
