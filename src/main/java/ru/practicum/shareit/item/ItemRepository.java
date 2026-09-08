package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item save(Item item);

    Item findById(long itemId);

    List<Item> findByOwnerId(long ownerId);

    List<Item> search(String text);

    Item update(Item item);
}