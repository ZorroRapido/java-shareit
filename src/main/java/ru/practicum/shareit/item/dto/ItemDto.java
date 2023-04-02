package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

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

    @JsonIgnore
    private UserDto owner;

    private Long requestId;

    private BookingShortDto lastBooking;

    private BookingShortDto nextBooking;

    private List<CommentDto> comments;

    public ItemDto(Long id, String name, String description, Boolean available, UserDto owner, List<CommentDto> comments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
        this.comments = comments;
    }
}
