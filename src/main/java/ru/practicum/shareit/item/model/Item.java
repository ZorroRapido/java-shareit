package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class Item {
    private Long id;

    @NotEmpty(message = "Название (name) вещи не должно быть пустым!")
    private String name;

    @NotEmpty(message = "Описание (description) вещи не должно быть пустым!")
    private String description;

    @NotNull(message = "Не указан статус (available) вещи!")
    private Boolean available;

    private Long owner;

    private ItemRequest request;
}
