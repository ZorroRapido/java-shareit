package ru.practicum.shareit.booking.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.common.service.ConsistencyService;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @InjectMocks
    private ConsistencyService consistencyService;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private BookingRepository mockBookingRepository;

    @Test
    void shouldExceptionWhenUpdateStatusOfNotExistingBooking() {
        BookingService bookingService = new BookingServiceImpl(mockBookingRepository, null, null, null,
                consistencyService);

        when(mockUserRepository.existsById(any(Long.class)))
                .thenReturn(true);

        when(mockBookingRepository.existsById(any(Long.class)))
                .thenReturn(false);

        Long bookingId = 1L;

        final BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.updateStatus(bookingId, true, 2L));
        assertEquals(String.format("Бронирование с id = %d не найдено!", bookingId), exception.getMessage());
    }
}
