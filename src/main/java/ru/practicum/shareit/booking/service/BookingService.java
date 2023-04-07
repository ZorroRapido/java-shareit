package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.util.List;

@Service
public interface BookingService {

    BookingDto add(BookingInputDto bookingInputDto, Long userId);

    BookingDto updateStatus(Long bookingId, Boolean approved, Long userId);

    BookingDto get(Long bookingId, Long userId);

    List<BookingDto> getAllBookingsByUserId(String state, Integer from, Integer size, Long userId);

    List<BookingDto> getAllBookingsForUserItems(String state, Integer from, Integer size, Long userId);
}
