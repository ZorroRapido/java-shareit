package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ItemDto {

    private Long id;

    @NotEmpty(message = "Название (name) вещи не должно быть пустым!")
    private String name;

    @NotEmpty(message = "Описание (description) вещи не должно быть пустым!")
    private String description;

    @NotNull(message = "Не указан статус (available) вещи!")
    private Boolean available;

    private Long requestId;

    private BookingShortDto lastBooking;

    private BookingShortDto nextBooking;

    private List<CommentDto> comments;

    public ItemDto(Long id, String name, String description, Boolean available, List<CommentDto> comments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.comments = comments;
    }
}
