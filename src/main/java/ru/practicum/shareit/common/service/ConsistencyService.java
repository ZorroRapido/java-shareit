package ru.practicum.shareit.common.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ConsistencyService {

    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private BookingRepository bookingRepository;
    private ItemRequestRepository itemRequestRepository;

    public void checkStateExistence(String state) {
        var existingStates = Arrays.stream(State.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        if (!existingStates.contains(state)) {
            String errorMessage = String.format("Unknown state: %s", state);
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    public void checkUserExistence(Long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользователь c id = %d не найден!", userId);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
    }

    public void checkItemExistence(BookingInputDto bookingInputDto) {
        if (!itemRepository.existsById(bookingInputDto.getItemId())) {
            String errorMessage = String.format("Вещь с id = %d не найдена!", bookingInputDto.getItemId());
            log.warn(errorMessage);
            throw new ItemNotFoundException(errorMessage);
        }
    }

    public void checkItemExistence(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            String errorMessage = String.format("Вещь с id = %d не найдена!", itemId);
            log.warn(errorMessage);
            throw new ItemNotFoundException(errorMessage);
        }
    }

    public void checkBookingExistence(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            String errorMessage = String.format("Бронирование с id = %d не найдено!", bookingId);
            log.warn(errorMessage);
            throw new BookingNotFoundException(errorMessage);
        }
    }

    public void checkItemRequestExistence(Long requestId) {
        if (!itemRequestRepository.existsById(requestId)) {
            String errorMessage = String.format("Запрос c id = %d не найден!", requestId);
            log.warn(errorMessage);
            throw new ItemRequestNotFoundException(errorMessage);
        }
    }
}
