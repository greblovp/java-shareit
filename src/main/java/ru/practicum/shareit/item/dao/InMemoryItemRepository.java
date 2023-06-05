package ru.practicum.shareit.item.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;

import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Repository("inMemoryItemRepository")
@Slf4j
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    //Связь между пользователями и вещами key - ownerId, value - id (item)
    private final Map<Long, List<Long>> userItems = new HashMap<>();
    private long idCounter;

    @Override
    public Item createItem(Item item) {
        item.setId(++idCounter);
        long itemId = item.getId();
        long ownerId = item.getOwnerId();

        if (!userItems.containsKey(ownerId)) {
            userItems.put(ownerId, new ArrayList<>());
        }
        userItems.get(ownerId).add(itemId);
        items.put(itemId, item);
        log.info("Создана вещь: {}", item);
        return item;
    }

    @Override
    public Item patchItem(Item item) {
        Item newItem = items.get(item.getId());

        String name = item.getName();
        String description = item.getDescription();
        Boolean isAvailable = item.getIsAvailable();

        if (name != null) {
            newItem.setName(name);
        }
        if (description != null) {
            newItem.setDescription(description);
        }
        if (isAvailable != null) {
            newItem.setIsAvailable(isAvailable);
        }

        log.info("Вещь изменена: {}", newItem);
        return newItem;
    }

    @Override
    public Collection<Item> getItems(long userId) {
        return userItems.get(userId).stream()
                .map(items::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> getItemById(long id) {
        if (!items.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(items.get(id));
    }

    @Override
    public Collection<Item> searchItem(String text) {
        return items.values().stream()
                .filter(Item::getIsAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }
}
