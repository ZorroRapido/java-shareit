package ru.practicum.shareit.booking.unit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    private final User user1 = new User(300L, "user1", "first@user.ru");
    private final User user2 = new User(301L, "user2", "second@user.ru");
    private final User user3 = new User(302L, "user3", "third@user.ru");

    private final ItemDto itemDto1 = new ItemDto(301L, "item1", "description1", true,
            null, null);

    @Test
    void shouldExceptionWhenCreateBookingByOwnerItem() {
        UserDto ownerDto = userService.create(user1);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        BookingNotFoundException exp = assertThrows(BookingNotFoundException.class,
                () -> bookingService.add(bookingInputDto, ownerDto.getId()));
        assertEquals("Вы не можете забронировать вещь, для которой являетесь владельцем!",
                exp.getMessage());
    }

    @Test
    void shouldStatusRejectedWhenUpdateStatusByOwnerAndApprovedFalse() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());
        bookingDto = bookingService.updateStatus(bookingDto.getId(), false, ownerDto.getId());

        assertEquals(Status.REJECTED, bookingDto.getStatus());
    }

    @Test
    void shouldExceptionWhenUpdateStatusByOwnerAndStatusRejected() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());
        bookingDto = bookingService.updateStatus(bookingDto.getId(), false, ownerDto.getId());
        Long bookingId = bookingDto.getId();

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.updateStatus(bookingId, false, ownerDto.getId()));
        assertEquals(String.format("Невозможно изменить статус для бронирования с id = %d!", bookingId),
                exp.getMessage());
    }

    @Test
    void shouldExceptionWhenGetBookingByNotOwnerOrNotBooker() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        UserDto otherUserDto = userService.create(user3);

        Long otherUserId = otherUserDto.getId();
        ItemDto newItemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());

        BookingNotFoundException exp = assertThrows(BookingNotFoundException.class,
                () -> bookingService.get(bookingDto.getId(), otherUserId));
        assertEquals(String.format("У пользователя с id = %d нет прав для просмотра информации о бронировании с id = %d!",
                otherUserId, bookingDto.getId()), exp.getMessage());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("ALL", 0, null,
                bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByBookerAndSizeIsNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("ALL", 0, 1,
                bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInWaitingStatusByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("WAITING", null,
                0, bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInWaitingStatusByBookerAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("WAITING",
                0, 1, bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInRejectedStatusByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("REJECTED",
                0, null, bookerDto.getId());
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInRejectedStatusByBookerAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("REJECTED", 0, 1,
                bookerDto.getId());
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingWhenGetAllBookingsInCurrentStateByBookerAndSizeOne() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusYears(1));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("CURRENT", 0, 1,
                bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingWhenGetAllBookingsInPastStateByBookerAndSizeTwo() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusMinutes(10));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusYears(1));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("CURRENT", 0, 2,
                bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingWhenGetAllBookingsInFutureStateByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusYears(1));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("FUTURE", 0, null,
                bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("ALL", 0, null,
                ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("ALL", 0, 1,
                ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusWaitingAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("WAITING", 0, null,
                ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusWaitingAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("WAITING", 0, 1,
                ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusRejectedAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("REJECTED", 0, null,
                ownerDto.getId());
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusRejectedAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("REJECTED", 0, 1,
                ownerDto.getId());
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusCurrentAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5));
        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now().plusMinutes(50));
        BookingDto bookingDto1 = bookingService.add(bookingInputDto1, bookerDto.getId());

        bookingService.updateStatus(bookingDto.getId(), true, ownerDto.getId());
        bookingService.updateStatus(bookingDto1.getId(), true, ownerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("CURRENT", 0, null,
                ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingWhenGetBookingsByOwnerAndStatusPastAndSizeIsOne() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1));
        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now().plusMinutes(50));
        BookingDto bookingDto1 = bookingService.add(bookingInputDto1, bookerDto.getId());

        bookingService.updateStatus(bookingDto.getId(), true, ownerDto.getId());
        bookingService.updateStatus(bookingDto1.getId(), true, ownerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("PAST", 0, 2,
                ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldExceptionWhenGetAllBookingsInUnknownStateByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusYears(1));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsByUserId("UNKNOWN_STATE", 0, null, bookerDto.getId()));
        assertEquals("Unknown state: UNKNOWN_STATE", exp.getMessage());
    }
}