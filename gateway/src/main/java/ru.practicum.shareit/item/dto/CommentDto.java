package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {

    private Long id;

    @NotEmpty(message = "Текст комментария (text) не должен быть пустым!")
    private String text;

    private String authorName;

    private LocalDateTime created;
}
