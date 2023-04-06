package ru.practicum.shareit.request.unit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {

    private final ItemRequestService itemRequestService;
    private final UserService userService;

    private final User user1 = new User(101L, "AlexOne", "alexone@alex.ru");
    private final UserDto userDto1 = new UserDto(101L, "AlexOne", "alexone@alex.ru");
    private final User user2 = new User(102L, "AlexTwo", "alextwo@alex.ru");

    private final ItemRequestDto itemRequestDto = new ItemRequestDto(100L, "ItemRequest description", userDto1,
            LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

    @Test
    void shouldCreateItemRequest() {
        UserDto newUserDto = userService.create(user1);
        ItemRequestDto returnRequestDto = itemRequestService.create(itemRequestDto, newUserDto.getId());

        assertThat(returnRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldExceptionWhenCreateItemRequestWithWrongUserId() {
        UserNotFoundException exp = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.create(itemRequestDto, -2L));

        assertEquals("Пользователь c id = -2 не найден!", exp.getMessage());
    }

    @Test
    void shouldExceptionWhenGetItemRequestWithWrongId() {
        UserDto userDto = userService.create(user1);

        ItemRequestNotFoundException exp = assertThrows(ItemRequestNotFoundException.class,
                () -> itemRequestService.getById(-2L, userDto.getId()));
        assertEquals("Запрос c id = -2 не найден!", exp.getMessage());
    }

    @Test
    void shouldReturnAllItemRequestsWhenSizeNotNull() {
        UserDto userDto = userService.create(user1);
        UserDto requesterDto = userService.create(user2);

        itemRequestService.create(itemRequestDto, requesterDto.getId());
        itemRequestService.create(itemRequestDto, requesterDto.getId());
        List<ItemRequestDto> listItemRequest = itemRequestService.getAll(0, 10, userDto.getId());

        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldReturnAllItemRequestsWhenSizeIsNull() {
        UserDto userDto = userService.create(user1);
        UserDto requesterDto = userService.create(user2);

        itemRequestService.create(itemRequestDto, requesterDto.getId());
        itemRequestService.create(itemRequestDto, requesterDto.getId());
        List<ItemRequestDto> listItemRequest = itemRequestService.getAll(0, null, userDto.getId());

        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldExceptionWhenGetAllItemRequestsAndSizeIsNegative() {
        UserDto userDto = userService.create(user1);
        UserDto requesterDto = userService.create(user2);

        itemRequestService.create(itemRequestDto, requesterDto.getId());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.getAll(0, -1, userDto.getId()));
        assertEquals("Параметр size должен быть больше 0 или равен null!", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenGetAllItemRequestsAndSizeIsZero() {
        UserDto userDto = userService.create(user1);
        UserDto requesterDto = userService.create(user2);

        itemRequestService.create(itemRequestDto, requesterDto.getId());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.getAll(0, 0, userDto.getId()));
        assertEquals("Параметр size должен быть больше 0 или равен null!", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenGetAllItemRequestsAndFromIsNegative() {
        UserDto userDto = userService.create(user1);
        UserDto requesterDto = userService.create(user2);

        itemRequestService.create(itemRequestDto, requesterDto.getId());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.getAll(-1, null, userDto.getId()));
        assertEquals("Параметр from должен быть >= 0 или равен null!", exception.getMessage());
    }

    @Test
    void shouldReturnOwnItemRequests() {
        UserDto userDto = userService.create(user2);

        itemRequestService.create(itemRequestDto, userDto.getId());
        itemRequestService.create(itemRequestDto, userDto.getId());
        List<ItemRequestDto> listItemRequest = itemRequestService.getOwn(userDto.getId());

        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldReturnItemRequestById() {
        UserDto userDto = userService.create(user1);

        ItemRequestDto newItemRequestDto = itemRequestService.create(itemRequestDto, userDto.getId());
        ItemRequestDto returnItemRequestDto = itemRequestService.getById(newItemRequestDto.getId(), userDto.getId());

        assertThat(returnItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }
}