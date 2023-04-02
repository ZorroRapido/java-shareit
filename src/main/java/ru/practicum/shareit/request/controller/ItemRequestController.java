package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createItemRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                                            @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(itemRequestService.create(itemRequestDto, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getOwnItemRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(itemRequestService.getOwn(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllItemRequests(@RequestParam(value = "from", required = false) Integer from,
                                                                   @RequestParam(value = "size", required = false) Integer size,
                                                                   @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(itemRequestService.getAll(from, size, userId));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getItemRequestById(@PathVariable("requestId") Long requestId,
                                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity.ok().body(itemRequestService.getById(requestId, userId));
    }
}
