package ru.practicum.shareit.item.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    private ItemRepository mockItemRepository;

    @Test
    void shouldExceptionWhenGetItemWithWrongId() {
        ItemService itemService = new ItemServiceImpl(null, mockItemRepository, null, null,
                null, null, null);

        when(mockItemRepository.existsById(any(Long.class)))
                .thenReturn(false);

        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.get(-1L, 1L));
        assertEquals("Вещь с id = -1 не найдена!", exception.getMessage());
    }
}
