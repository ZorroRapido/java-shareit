package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemRequestDto {

    private Long id;

    @NotBlank(message = "Описание запроса (description) не должно быть пустым!")
    private String description;

    private UserDto requester;

    private LocalDateTime created;

    private List<ItemDto> items;
}
