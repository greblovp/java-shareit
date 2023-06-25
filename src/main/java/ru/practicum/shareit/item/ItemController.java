package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.exception.CommentValidationException;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;

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
    public Collection<ItemExtendedDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Вывести все вещи пользователя ID = {}", userId);
        return itemService.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemExtendedDto getItemById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        log.info("Вывести вещь ID = {}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(@RequestParam String text) {
        log.info("Вывести вещи, содержащие в названии или описании текст {}", text);
        return itemService.searchItem(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable long itemId,
                                 @RequestBody @Valid CommentDto commentDto, BindingResult bindingResult) {
        log.info("Добавить комментарий к вещи ID = {}", itemId);
        generateCustomValidateException(commentDto, bindingResult);
        return itemService.addComment(userId, itemId, commentDto);
    }

    private void generateCustomValidateException(CommentDto commentDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Комментарий - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), commentDto);
            throw new CommentValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField());
        }
    }

}
