package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto add(Item item, Long userId);

    ItemDto edit(Item item, Long itemId, Long userId);

    ItemDto get(Long itemId);

    List<ItemDto> getAll(Long userId);

    List<ItemDto> search(String text);
}
