package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Service
public interface ItemService {

    ItemDto add(ItemDto itemDto, Long userId);

    ItemDto edit(ItemDto itemDto, Long itemId, Long userId);

    ItemDto get(Long itemId, Long userId);

    List<ItemDto> getAll(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(CommentDto comment, Long itemId, Long userId);
}
