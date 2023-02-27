package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item save(Item item);

    Item update(Item item, Long itemId);

    Item get(Long itemId);

    List<Item> getAllByUserId(Long userId);

    List<Item> search(String text);

    List<Long> getIdsList();
}
