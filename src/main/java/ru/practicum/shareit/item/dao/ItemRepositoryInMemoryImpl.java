package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryInMemoryImpl implements ItemRepository {
    private final Map<Long, Item> items;
    private static long counter;

    @Override
    public Item save(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item, Long itemId) {
        Item oldItem = get(itemId);

        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }

        return oldItem;
    }

    @Override
    public Item get(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAllByUserId(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        return items.values().stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text) ||
                        item.getDescription().toLowerCase().contains(text))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getIdsList() {
        return new ArrayList<>(items.keySet());
    }

    private long getNextId() {
        return ++counter;
    }
}
