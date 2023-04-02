package ru.practicum.shareit.request.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository mockItemRequestRepository;

    @Mock
    private UserRepository mockUserRepository;

    private ItemRequestService itemRequestService;

    private final UserDto userDto = new UserDto(1L, "Alex", "alex@alex.ru");

    private final ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "ItemRequest description",
            userDto, LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

    @BeforeEach
    void beforeEach() {
        itemRequestService = new ItemRequestServiceImpl(mockItemRequestRepository, mockUserRepository, null);
    }

    @Test
    void shouldExceptionWhenGetItemRequestWithWrongId() {
        when(mockUserRepository.existsById(any(Long.class)))
                .thenReturn(true);

        when(mockItemRequestRepository.existsById(any(Long.class)))
                .thenReturn(false);

        final ItemRequestNotFoundException exception = Assertions.assertThrows(ItemRequestNotFoundException.class,
                () -> itemRequestService.getById(-1L, 1L));
        assertEquals("Запрос c id = -1 не найден!", exception.getMessage());
    }
}