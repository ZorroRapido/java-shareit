package ru.practicum.shareit.booking.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
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

    @Mock
    BookingRepository mockBookingRepository;

    @Mock
    UserRepository mockUserRepository;

    @Test
    void shouldExceptionWhenGetBookingWithWrongId() {
        BookingService bookingService = new BookingServiceImpl(mockBookingRepository, null, mockUserRepository,
                null);

        when(mockUserRepository.existsById(any(Long.class)))
                .thenReturn(true);

        when(mockBookingRepository.existsById(any(Long.class)))
                .thenReturn(false);

        final BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.get(-1L, 1L));

        assertEquals("Бронирование с id = -1 не найдено!", exception.getMessage());
    }
}