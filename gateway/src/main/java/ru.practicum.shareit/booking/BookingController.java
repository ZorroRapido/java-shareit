package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Validated
@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingClient bookingClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Пользователь с id = {} отправил запрос на создание бронирования: {}", userId, requestDto);
        return bookingClient.createBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(@PathVariable("bookingId") Long bookingId,
                                                      @RequestParam("approved") Boolean approved,
                                                      @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id = {} отправил запрос на изменение статуса на {} для бронирования с id = {}", userId,
                approved, bookingId);
        return bookingClient.updateBookingStatus(userId, approved, bookingId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("Пользователь с id = {} отправил запрос на получение бронирования с id = {}", userId, bookingId);
        return bookingClient.getBooking(userId, bookingId);
    }

    // TODO: убрать defaultValue
    @GetMapping
    public ResponseEntity<Object> getAllBookingsByUserId(@RequestHeader(USER_ID_HEADER) Long userId,
                                                         @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Пользователь с id = {} отправил запрос на получение списка бронирований со state = {} (from = {}," +
                " size = {})", userId, stateParam, from, size);
        return bookingClient.getAllBookingsByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsForUserItems(@RequestHeader(USER_ID_HEADER) Long userId,
                                                          @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                          @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                          @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Пользователь с id = {} отправил запрос на получение списка бронирований co state = {} на вещи, для " +
                "которых он является владельцем (from = {}, size = {})", userId, stateParam, from, size);
        return bookingClient.getBookingForUserItems(userId, state, from, size);
    }
}