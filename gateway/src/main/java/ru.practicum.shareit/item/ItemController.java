package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;

@Slf4j
@Validated
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemDto itemDto,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id = {} отправил запрос на создание вещи: {}", userId, itemDto);
        return itemClient.createItem(userId, itemDto);
    }


    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto, @PathVariable("itemId") Long itemId,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id = {} отправил запрос на обновление вещи c id = {}: {}", userId, itemId, itemDto);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable("itemId") Long itemId,
                                              @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id = {} отправил запрос на получение вещи с id = {}", userId, itemId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(@RequestParam(name = "from", required = false) Integer from,
                                                   @RequestParam(name = "size", required = false) Integer size,
                                                   @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id = {} отправил запрос на получение списка своих вещей (from = {}, size = {})",
                userId, from, size);
        return itemClient.getItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchForItem(@RequestParam(name = "from", required = false) Integer from,
                                                @RequestParam(name = "size", required = false) Integer size,
                                                @RequestParam("text") String text,
                                                @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id = {} отправил запрос на поиск вещей с text = {} (from = {}, size = {})", userId,
                text, from, size);
        return itemClient.searchForItem(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@Valid @RequestBody CommentDto commentDto,
                                                @PathVariable("itemId") Long itemId,
                                                @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id = {} отправил запрос на создание комментария на вещь с id = {}: {}", userId,
                itemId, commentDto);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}
