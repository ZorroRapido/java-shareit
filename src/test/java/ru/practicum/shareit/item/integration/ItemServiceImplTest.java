package ru.practicum.shareit.item.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.common.service.ConsistencyService;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @InjectMocks
    private ConsistencyService consistencyService;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private ItemRepository mockItemRepository;

    @Test
    void shouldExceptionWhenEditNotExistingItem() {
        ItemService itemService = new ItemServiceImpl(consistencyService, null, null, mockItemRepository,
                mockUserRepository, null, null, null);

        when(mockUserRepository.existsById(any(Long.class)))
                .thenReturn(true);

        when(mockItemRepository.existsById(any(Long.class)))
                .thenReturn(false);

        ItemDto itemDto = new ItemDto(1L, "item1", "description1", true, null, null);

        final ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> itemService.edit(itemDto, itemDto.getId(), 2L));
        assertEquals(String.format("Вещь с id = %d не найдена!", itemDto.getId()), exception.getMessage());
    }
}
