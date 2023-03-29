package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotEnoughRightsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.APPROVED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public ItemDto add(ItemDto itemDto, Long userId) {
        checkUserExistence(userId);

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userRepository.getReferenceById(userId));

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public ItemDto edit(ItemDto itemDto, Long itemId, Long userId) {
        checkUserExistence(userId);
        checkItemExistence(itemId);

        Item item = itemRepository.getReferenceById(itemId);

        if (!userId.equals(item.getOwner().getId())) {
            String errorMessage = String.format("У пользователя c id = %d нет вещи с id = %d!", userId, itemId);
            log.warn(errorMessage);
            throw new ItemNotFoundException(errorMessage);
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto get(Long itemId, Long userId) {
        checkItemExistence(itemId);

        Item item = itemRepository.getReferenceById(itemId);
        ItemDto itemDto = itemMapper.toItemDto(item);

        if (isOwner(userId, itemId)) {
            LocalDateTime dateTime = LocalDateTime.now();

            itemDto.setLastBooking(bookingMapper.toBookingShortDto(getLastBooking(itemId, dateTime)));
            itemDto.setNextBooking(bookingMapper.toBookingShortDto(getNextBooking(itemId, dateTime)));
        }

        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getAll(Long userId) {
        List<ItemDto> allItems = itemRepository.findByOwnerId(userId).stream()
                .map(itemMapper::toItemDto)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());

        LocalDateTime dateTime = LocalDateTime.now();

        for (ItemDto itemDto : allItems) {
            Booking lastBooking = getLastBooking(itemDto.getId(), dateTime);
            Booking nextBooking = getNextBooking(itemDto.getId(), dateTime);

            itemDto.setLastBooking(lastBooking != null ? bookingMapper.toBookingShortDto(lastBooking) : null);
            itemDto.setNextBooking(nextBooking != null ? bookingMapper.toBookingShortDto(nextBooking) : null);

            itemDto.setComments(commentRepository.findCommentsByItemId(itemDto.getId()).stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        }

        return allItems;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.search(text.toLowerCase()).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(CommentDto commentDto, Long itemId, Long userId) {
        checkUserExistence(userId);

        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findPastAndCurrentActiveBookingsByBookerIdAndItemId(userId, itemId,
                dateTime);

        if (bookings.isEmpty()) {
            String errorMessage = String.format("Пользователь с id = %d никогда не бронировал вещь с id = %d!", userId,
                    itemId);
            log.warn(errorMessage);
            throw new NotEnoughRightsException(errorMessage);
        }

        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(itemRepository.getReferenceById(itemId));
        comment.setAuthor(userRepository.getReferenceById(userId));
        comment.setCreated(dateTime);

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void checkUserExistence(Long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользовать с id = %d не найден!", userId);
            log.warn(errorMessage);
            throw new UserNotFoundException(String.format(errorMessage));
        }
    }

    private void checkItemExistence(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            String errorMessage = String.format("Вещь с id = %d не найдена!", itemId);
            log.warn(errorMessage);
            throw new ItemNotFoundException(errorMessage);
        }
    }

    private boolean isOwner(Long userId, Long itemId) {
        return itemRepository.getReferenceById(itemId).getOwner().getId().equals(userId);
    }

    private Booking getLastBooking(Long itemId, LocalDateTime dateTime) {
        List<Booking> approvedItemBookings = bookingRepository.findAllBookingsByItemId(itemId).stream()
                .filter(booking -> APPROVED.equals(booking.getStatus()))
                .collect(Collectors.toList());

        return approvedItemBookings.stream()
                .filter(booking -> booking.getStart().isBefore(dateTime))
                .max(Comparator.comparing(Booking::getStart))
                .orElse(null);
    }

    private Booking getNextBooking(Long itemId, LocalDateTime dateTime) {
        List<Booking> approvedItemBookings = bookingRepository.findAllBookingsByItemId(itemId).stream()
                .filter(booking -> APPROVED.equals(booking.getStatus()))
                .collect(Collectors.toList());

        return approvedItemBookings.stream()
                .filter(booking -> booking.getStart().isAfter(dateTime))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
    }
}
