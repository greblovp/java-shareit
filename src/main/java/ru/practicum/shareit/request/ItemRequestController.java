package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.ItemRequestValidationException;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestBody @Valid ItemRequestDto itemRequestDto,
                                            BindingResult bindingResult) {
        log.info("Создать запрос на вещь \"{}\" для пользователя ID = {}", itemRequestDto.getDescription(), userId);
        generateCustomValidateException(itemRequestDto, bindingResult);
        return itemRequestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public Collection<ItemRequestDto> getItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получить все запросы на вещи для пользователя ID = {}", userId);
        return itemRequestService.getItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long requestId) {
        log.info("Получить запрос на вещь ID = {} для пользователя ID = {}", requestId, userId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получить все запросы на вещи для пользователя ID = {} с пагинацией", userId);
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    private void generateCustomValidateException(ItemRequestDto itemRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. Запрос - {}", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage(), itemRequestDto);
            throw new ItemRequestValidationException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField() + " - " +
                    bindingResult.getFieldError().getDefaultMessage());
        }
    }
}
