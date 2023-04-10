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
import ru.practicum.shareit.exception.NotAvailableForBookingException;
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

    private final ItemDto itemDto1 = new ItemDto(301L, "item1", "description1", true, null, null);

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
        assertEquals("Вы не можете забронировать вещь, для которой являетесь владельцем!", exp.getMessage());
    }

    @Test
    void shouldExceptionWhenUpdateStatusByBooker() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());

        BookingNotFoundException exp = assertThrows(BookingNotFoundException.class,
                () -> bookingService.updateStatus(bookingDto.getId(), true, bookerDto.getId()));
        assertEquals(String.format("Статус бронирования может изменить только владелец вещи с id = %d!",
                itemDto.getId()), exp.getMessage());
    }

    @Test
    void shouldSetStatusToApprovedWhenUpdateStatusByOwnerAndApprovedTrue() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        BookingDto bookingDto = bookingService.add(bookingInputDto, bookerDto.getId());
        bookingDto = bookingService.updateStatus(bookingDto.getId(), true, ownerDto.getId());

        assertEquals(Status.APPROVED, bookingDto.getStatus());
    }

    @Test
    void shouldSetStatusToRejectedWhenUpdateStatusByOwnerAndApprovedFalse() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
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

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
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
    void shouldExceptionWhenGetAllBookingsInUnknownStateByUserId() {
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

    @Test
    void shouldReturnBookingsWhenGetAllBookingsByUserIdAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        BookingInputDto bookingInputDto2 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto2, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("ALL", 0, null, bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsByUserIdAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusYears(10),
                LocalDateTime.now().minusYears(5));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        BookingInputDto bookingInputDto2 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().plusYears(1),
                LocalDateTime.now().plusYears(2));
        bookingService.add(bookingInputDto2, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("ALL", 0, 1, bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInCurrentStateByUserIdAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("CURRENT", 0, null, bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInCurrentStateByUserIdAndSizeNotNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("CURRENT", 0, 1, bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInPastStateByUserIdAndSizeIsNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusYears(10),
                LocalDateTime.now().minusYears(5));
        BookingDto bookingDto1 = bookingService.add(bookingInputDto1, bookerDto.getId());

        BookingInputDto bookingInputDto2 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusYears(2),
                LocalDateTime.now().minusYears(1));
        BookingDto bookingDto2 = bookingService.add(bookingInputDto2, bookerDto.getId());

        bookingService.updateStatus(bookingDto1.getId(), true, ownerDto.getId());
        bookingService.updateStatus(bookingDto2.getId(), true, ownerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("PAST", 0, null, bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInPastStateByUserIdAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusYears(10),
                LocalDateTime.now().minusYears(5));
        BookingDto bookingDto1 = bookingService.add(bookingInputDto1, bookerDto.getId());

        BookingInputDto bookingInputDto2 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusYears(2),
                LocalDateTime.now().minusYears(1));
        bookingService.add(bookingInputDto1, bookerDto.getId());
        BookingDto bookingDto2 = bookingService.add(bookingInputDto2, bookerDto.getId());

        bookingService.updateStatus(bookingDto1.getId(), true, ownerDto.getId());
        bookingService.updateStatus(bookingDto2.getId(), true, ownerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("PAST", 0, 1, bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInFutureStateByUserIdAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("FUTURE", 0, null, bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInFutureStateByUserIdAndSizeNotNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("FUTURE", 0, 1, bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInWaitingStateByUserIdAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("WAITING", 0, null, bookerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInWaitingStateByUserIdAndSizeNotNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("WAITING", 0, 1, bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnNoBookingsWhenGetAllBookingsInRejectedStateByUserIdAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("REJECTED", 0, null, bookerDto.getId());
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsInRejectedStateByUserIdAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        BookingInputDto bookingInputDto2 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        BookingDto bookingDto2 = bookingService.add(bookingInputDto2, bookerDto.getId());

        bookingService.updateStatus(bookingDto2.getId(), false, ownerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsByUserId("REJECTED", 0, 1, bookerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("ALL", 0, null, ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndSizeNotNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("ALL", 0, 1, ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateCurrentAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("CURRENT", 0, null, ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateCurrentAndSizeNotNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("CURRENT", 0, 1, ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStatePastAndSizeIsNull() {
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
                LocalDateTime.now().minusDays(1));
        BookingDto bookingDto1 = bookingService.add(bookingInputDto1, bookerDto.getId());

        bookingService.updateStatus(bookingDto.getId(), true, ownerDto.getId());
        bookingService.updateStatus(bookingDto1.getId(), true, ownerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("PAST", 0, null, ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStatePastAndSizeNotNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("PAST", 0, 2, ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateFutureAndSizeIsNull() {
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
                LocalDateTime.now().plusMonths(2),
                LocalDateTime.now().plusMonths(10));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("FUTURE", 0, null, ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateFutureAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2022, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto, bookerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("FUTURE", 0, 1, ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateWaitingAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("WAITING", 0, null, ownerDto.getId());
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateWaitingAndSizeNotNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("WAITING", 0, 1, ownerDto.getId());
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateRejectedAndSizeIsNull() {
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

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("REJECTED", 0, null, ownerDto.getId());
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetAllBookingsForUserItemsAndStateRejectedAndSizeNotNull() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto1 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto1, bookerDto.getId());

        BookingInputDto bookingInputDto2 = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.add(bookingInputDto2, bookerDto.getId());

        List<BookingDto> listBookings = bookingService.getAllBookingsForUserItems("REJECTED", 0, 1, ownerDto.getId());
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldExceptionWhenItemIsNotAvailableForBooking() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);

        itemDto1.setAvailable(Boolean.FALSE);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        NotAvailableForBookingException exp = assertThrows(NotAvailableForBookingException.class,
                () -> bookingService.add(bookingInputDto, bookerDto.getId()));
        assertEquals(String.format("Вещь с id = %d недоступна для бронирования!", itemDto.getId()), exp.getMessage());
    }

    @Test
    void shouldExceptionWhenBookingEndIsBeforeStart() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(10));

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.add(bookingInputDto, bookerDto.getId()));
        assertEquals("Дата окончания бронирования не может быть раньше или равняться дате начала бронирования!",
                exp.getMessage());
    }

    @Test
    void shouldExceptionWhenBookingEndEqualsStart() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        LocalDateTime currentDateTime = LocalDateTime.now();
        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                currentDateTime,
                currentDateTime);

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.add(bookingInputDto, bookerDto.getId()));
        assertEquals("Дата окончания бронирования не может быть раньше или равняться дате начала бронирования!",
                exp.getMessage());
    }

    @Test
    void shouldWhenGetAllBookingsForUserItemsAndFromIsNegative() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        bookingService.add(bookingInputDto, bookerDto.getId());

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsForUserItems("ALL", -1, 2, ownerDto.getId()));
        assertEquals("Параметр from должен быть >= 0 или равен null!", exp.getMessage());
    }

    @Test
    void shouldWhenGetAllBookingsForUserItemsAndSizeIsNegative() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        bookingService.add(bookingInputDto, bookerDto.getId());

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsForUserItems("ALL", 0, -2, ownerDto.getId()));
        assertEquals("Параметр size должен быть больше 0 или равен null!", exp.getMessage());
    }

    @Test
    void shouldWhenGetAllBookingsForUserItemsAndSizeIsZero() {
        UserDto ownerDto = userService.create(user1);
        UserDto bookerDto = userService.create(user2);
        ItemDto itemDto = itemService.add(itemDto1, ownerDto.getId());

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));

        bookingService.add(bookingInputDto, bookerDto.getId());

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsForUserItems("ALL", 0, 0, ownerDto.getId()));
        assertEquals("Параметр size должен быть больше 0 или равен null!", exp.getMessage());
    }
}