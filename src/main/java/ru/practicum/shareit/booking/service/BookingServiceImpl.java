package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotAvailableForBookingException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.APPROVED;
import static ru.practicum.shareit.booking.model.Status.REJECTED;
import static ru.practicum.shareit.booking.model.Status.WAITING;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public BookingDto add(BookingInputDto bookingInputDto, Long userId) {
        checkUserExistence(userId);
        checkItemExistence(bookingInputDto);

        validate(bookingInputDto, userId);

        Booking booking = bookingMapper.toBooking(bookingInputDto);
        booking.setStatus(Status.WAITING);
        booking.setItem(itemRepository.getReferenceById(bookingInputDto.getItemId()));
        booking.setBooker(userRepository.getReferenceById(userId));

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingDto updateStatus(Long bookingId, Boolean approved, Long userId) {
        checkBookingExistence(bookingId);

        Booking booking = bookingRepository.getReferenceById(bookingId);

        if (!isOwner(userId, booking)) {
            String errorMessage = String.format("У пользователя c id = %d нет вещи c id = %d!", userId,
                    booking.getItem().getId());
            log.warn(errorMessage);
            throw new ItemNotFoundException(errorMessage);
        }

        if (WAITING.equals(booking.getStatus()) && approved) {
            booking.setStatus(APPROVED);
        } else if (WAITING.equals(booking.getStatus())) {
            booking.setStatus(REJECTED);
        } else {
            String errorMessage = String.format("Невозможно изменить статус для бронирования с id = %d!", bookingId);
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto get(Long bookingId, Long userId) {
        checkUserExistence(userId);
        checkBookingExistence(bookingId);

        Booking booking = bookingRepository.getReferenceById(bookingId);

        if (!isBooker(userId, booking) && !isOwner(userId, booking)) {
            String errorMessage = String.format("У пользователя с id = %d нет прав для просмотра информации " +
                    "о бронировании с id = %d!", userId, booking.getId());
            log.warn(errorMessage);
            throw new BookingNotFoundException(errorMessage);
        }

        return bookingMapper.toBookingDto(bookingRepository.getReferenceById(bookingId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllBookingsByUserId(Long userId, String state) {
        checkUserExistence(userId);
        checkStateExistence(state);

        LocalDateTime dateTime = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        switch (State.valueOf(state)) {
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, dateTime, dateTime,
                                sort).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByBookerIdAndStatusAndEndIsBefore(userId, APPROVED, dateTime, sort).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByBookerIdAndStatusInAndStartIsAfter(userId, List.of(APPROVED, WAITING),
                                dateTime, sort).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, sort).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, sort).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findByBookerId(userId, sort).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllBookingsForUserItems(Long userId, String state) {
        checkUserExistence(userId);
        checkStateExistence(state);

        LocalDateTime dateTime = LocalDateTime.now();

        switch (State.valueOf(state)) {
            case CURRENT:
                return bookingRepository.findAllBookingsByOwner(userId).stream()
                        .filter(booking -> booking.getStart().isBefore(dateTime) && booking.getEnd().isAfter(dateTime))
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllBookingsByOwner(userId, List.of(APPROVED)).stream()
                        .filter(booking -> booking.getEnd().isBefore(dateTime))
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllBookingsByOwner(userId, List.of(APPROVED, WAITING)).stream()
                        .filter(booking -> booking.getStart().isAfter(dateTime))
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllBookingsByOwner(userId, List.of(WAITING)).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllBookingsByOwner(userId, List.of(REJECTED)).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllBookingsByOwner(userId).stream()
                        .map(bookingMapper::toBookingDto)
                        .collect(Collectors.toList());
        }
    }

    private void checkStateExistence(String state) {
        var existingStates = Arrays.stream(State.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        if (!existingStates.contains(state)) {
            String errorMessage = String.format("Unknown state: %s", state);
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    private void checkUserExistence(Long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользователь c id = %d не найден!", userId);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
    }

    private void checkItemExistence(BookingInputDto bookingInputDto) {
        if (!itemRepository.existsById(bookingInputDto.getItemId())) {
            String errorMessage = String.format("Вещь с id = %d не найдена!", bookingInputDto.getItemId());
            log.warn(errorMessage);
            throw new ItemNotFoundException(errorMessage);
        }
    }

    private void checkBookingExistence(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            String errorMessage = String.format("Бронирование с id = %d не найдено!", bookingId);
            log.warn(errorMessage);
            throw new BookingNotFoundException(errorMessage);
        }
    }

    private boolean isOwner(Long userId, Booking booking) {
        return itemRepository.getReferenceById(booking.getItem().getId()).getOwner().getId().equals(userId);
    }

    private boolean isBooker(Long userId, Booking booking) {
        return booking.getBooker().getId().equals(userId);
    }

    private void validate(BookingInputDto bookingInputDto, Long userId) {
        Item item = itemRepository.getReferenceById(bookingInputDto.getItemId());

        if (Boolean.FALSE.equals(item.getAvailable())) {
            String errorMessage = String.format("Вещь с id = %d недоступна для бронирования!", bookingInputDto.getItemId());
            log.warn(errorMessage);
            throw new NotAvailableForBookingException(errorMessage);
        }

        if (itemRepository.getReferenceById(bookingInputDto.getItemId()).getOwner().getId().equals(userId)) {
            String errorMessage = "Вы не можете забронировать вещь, для которой являетесь владельцем!";
            log.warn(errorMessage);
            throw new BookingNotFoundException(errorMessage);
        }

        if (bookingInputDto.getEnd().isBefore(bookingInputDto.getStart())
                || bookingInputDto.getEnd().equals(bookingInputDto.getStart())) {
            log.warn("Дата окончания бронирования не может быть раньше или равняться дате начала бронирования!");
            throw new ValidationException("Дата окончания бронирования не может быть раньше или равняться дате " +
                    "начала бронирования!");
        }
    }
}
