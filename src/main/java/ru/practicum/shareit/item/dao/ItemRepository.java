package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Item createItem(Item item);

    Item patchItem(Item item);

    Collection<Item> getItems(long userId);

    Optional<Item> getItemById(long id);

    Collection<Item> searchItem(String text);
}
