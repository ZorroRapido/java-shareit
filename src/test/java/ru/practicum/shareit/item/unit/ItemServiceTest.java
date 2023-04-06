package ru.practicum.shareit.item.unit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotEnoughRightsException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    private final User user1 = new User(200L, "user1", "first@user.ru");
    private final User user2 = new User(201L, "user2", "second@user.ru");

    private final UserDto userDto1 = new UserDto(200L, "user1", "first@user.ru");
    private final UserDto userDto2 = new UserDto(201L, "user2", "second@user.ru");

    private final ItemDto itemDto1 = new ItemDto(301L, "itemDto1", "description1", true, userDto1, null);
    private final ItemDto itemDto2 = new ItemDto(302L, "itemDto2", "description2", true, userDto2, null);

    @Test
    void shouldCreateItem() {
        UserDto userDto = userService.create(user1);
        ItemDto itemDto = itemService.add(itemDto1, userDto.getId());
        ItemDto returnItemDto = itemService.get(itemDto.getId(), userDto.getId());

        assertThat(returnItemDto.getId(), notNullValue());
        assertThat(returnItemDto.getName(), equalTo(itemDto.getName()));
        assertThat(returnItemDto.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(returnItemDto.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void shouldEditItem() {
        UserDto userDto = userService.create(user1);
        ItemDto itemDto = itemService.add(itemDto1, userDto.getId());

        itemDto.setName("newName");
        itemDto.setDescription("newDescription");
        itemDto.setAvailable(false);

        ItemDto returnItemDto = itemService.edit(itemDto, itemDto.getId(), userDto.getId());

        assertThat(returnItemDto.getName(), equalTo("newName"));
        assertThat(returnItemDto.getDescription(), equalTo("newDescription"));
        assertFalse(returnItemDto.getAvailable());
    }

    @Test
    void shouldExceptionWhenUpdateItemNotOwner() {
        UserDto ownerDto = userService.create(user1);
        UserDto userDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        ItemNotFoundException exp = assertThrows(ItemNotFoundException.class,
                () -> itemService.edit(itemDto, itemDto.getId(), userDto.getId()));
        assertEquals(String.format("У пользователя c id = %d нет вещи с id = %d!", userDto.getId(), itemDto.getId()),
                exp.getMessage());
    }

    @Test
    void shouldReturnItemsByOwner() {
        UserDto ownerDto = userService.create(user1);
        itemService.add(itemDto1, ownerDto.getId());
        itemService.add(itemDto2, ownerDto.getId());

        List<ItemDto> listItems = itemService.getAll(0, 10, ownerDto.getId());
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldReturnItemsBySearch() {
        UserDto ownerDto = userService.create(user1);
        itemService.add(itemDto1, ownerDto.getId());
        itemService.add(itemDto2, ownerDto.getId());

        List<ItemDto> listItems = itemService.search("item", 0, 1);
        assertEquals(1, listItems.size());
    }

    @Test
    void shouldReturnEmptyListWhenSearchByEmptyString() {
        UserDto ownerDto = userService.create(user1);
        itemService.add(itemDto1, ownerDto.getId());
        itemService.add(itemDto2, ownerDto.getId());

        List<ItemDto> listItems = itemService.search("", 0, 1);
        assertEquals(0, listItems.size());
    }

    @Test
    void shouldReturnItemsBySearchWhenSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        itemService.add(itemDto1, ownerDto.getId());
        itemService.add(itemDto2, ownerDto.getId());

        List<ItemDto> listItems = itemService.search("item", 0, null);
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldExceptionWhenAddCommentWhenUserNotBooker() {
        UserDto ownerDto = userService.create(user1);
        UserDto newUserDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        CommentDto commentDto = new CommentDto(1L, "comment1", newUserDto.getName(), LocalDateTime.now());

        NotEnoughRightsException exp = assertThrows(NotEnoughRightsException.class,
                () -> itemService.addComment(commentDto, itemDto.getId(), newUserDto.getId()));
        assertEquals(String.format("Пользователь с id = %d никогда не бронировал вещь с id = %d!", newUserDto.getId(),
                itemDto.getId()), exp.getMessage());
    }

    @Test
    void shouldAddComment() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(itemDto.getId(), LocalDateTime.now().plusSeconds(1),
                LocalDateTime.now().plusSeconds(3));

        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());
        bookingService.updateStatus(bookingDto.getId(), true, ownerDto.getId());

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CommentDto commentDto = new CommentDto(1L, "comment1", bookerDto.getName(), LocalDateTime.now());
        itemService.addComment(commentDto, itemDto.getId(), bookerDto.getId());

        assertEquals(1, itemService.getAll(0, 10, ownerDto.getId()).get(0).getComments().size());
    }
}