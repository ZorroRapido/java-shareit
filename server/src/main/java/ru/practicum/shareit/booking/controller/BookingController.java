package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingDto> addBooking(@Valid @RequestBody BookingInputDto bookingInputDto,
                                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(bookingService.add(bookingInputDto, userId));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable("bookingId") Long bookingId,
                                                          @RequestParam("approved") Boolean approved,
                                                          @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(bookingService.updateStatus(bookingId, approved, userId));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable("bookingId") Long bookingId,
                                                     @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(bookingService.get(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookingsByUserId(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                                   @RequestParam(name = "from", required = false) Integer from,
                                                                   @RequestParam(name = "size", required = false) Integer size,
                                                                   @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(bookingService.getAllBookingsByUserId(state, from, size, userId));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getAllBookingsForUserItems(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                                       @RequestParam(name = "from", required = false) Integer from,
                                                                       @RequestParam(name = "size", required = false) Integer size,
                                                                       @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(bookingService.getAllBookingsForUserItems(state, from, size, userId));
    }
}
