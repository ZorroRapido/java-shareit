package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.UserNotOwnerException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto add(Item item, Long userId) {
        if (!userService.existsById(userId)) {
            String errorMessage = String.format("Пользовать с id = %d не найден!", userId);
            log.warn(errorMessage);
            throw new UserNotFoundException(String.format(errorMessage));
        }

        item.setOwner(userId);

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto edit(Item item, Long itemId, Long userId) {
        if (!existsById(itemId)) {
            String errorMessage = String.format("Вещь с id = %d не найдена!", itemId);
            log.warn(errorMessage);
            throw new ItemNotFoundException(errorMessage);
        }

        if (!userId.equals(itemRepository.get(itemId).getOwner())) {
            String errorMessage = String.format("Пользователь c id = %d не является владельцем вещи с id = %d!", userId,
                    itemId);
            log.warn(errorMessage);
            throw new UserNotOwnerException(errorMessage);
        }

        return itemMapper.toItemDto(itemRepository.update(item, itemId));
    }

    @Override
    public ItemDto get(Long itemId) {
        return itemMapper.toItemDto(itemRepository.get(itemId));
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        return itemRepository.getAllByUserId(userId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.search(text.toLowerCase()).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public boolean existsById(Long id) {
        return itemRepository.getIdsList().contains(id);
    }
}
