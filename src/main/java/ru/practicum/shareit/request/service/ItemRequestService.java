package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Service
public interface ItemRequestService {

    ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getOwn(Long userId);

    List<ItemRequestDto> getAll(Integer from, Integer size, Long userId);

    ItemRequestDto getById(Long requestId, Long userId);
}
