package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private static final long INITIAL_ID = 1L;

    private final List<Item> items = new ArrayList<>();
    private long nextId = INITIAL_ID;

    @Override
    public Item save(Item item) {
        item.setId(nextId++);
        items.add(item);
        return item;
    }

    @Override
    public Item findById(long itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Item> findByOwnerId(long ownerId) {
        return items.stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .toList();
    }

    @Override
    public List<Item> search(String text) {
        String searchText = text.toLowerCase();

        return items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        item.getName().toLowerCase().contains(searchText)
                                || item.getDescription().toLowerCase().contains(searchText))
                .toList();
    }

    @Override
    public Item update(Item item) {
        Item existingItem = findById(item.getId());

        if (existingItem != null) {
            existingItem.setName(item.getName());
            existingItem.setDescription(item.getDescription());
            existingItem.setAvailable(item.getAvailable());
        }

        return existingItem;
    }
}